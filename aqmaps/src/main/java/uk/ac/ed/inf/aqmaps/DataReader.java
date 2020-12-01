package uk.ac.ed.inf.aqmaps;

import java.net.http.HttpClient;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;;


// This class is a helper class that will help me visualise some of the stations and read the information from the server
public class DataReader {
	
	// This will store the values to colour - I've made it private because I do not want it to be accessed by other classes
	private static HashMap<Integer, String> valueColour = new HashMap<Integer, String>();
	
	// This will store the values to marker symbol
	// Although we have only two options, doing it this way with HashMap would make it easier for adding other symbols in the future.
	private static HashMap<Integer, String> valueSymbol = new HashMap<Integer, String>();
	

	private static void fillValueColour() {
		
        valueColour.put(0, "#00ff00");
        valueColour.put(32, "#40ff00");
        valueColour.put(64, "#80ff00");
        valueColour.put(96, "#c0ff00");
        valueColour.put(128, "#ffc000");
        valueColour.put(160, "#ff8000");
        valueColour.put(192, "#ff4000");
        valueColour.put(224, "#ff0000");
		
	}
	
	private static void fillValueSymbol() {
		
		valueSymbol.put(0, "lighthouse");
		valueSymbol.put(128, "danger");
		
	}
	
	public static Feature readStation(double[] coord, Station nearestSt) {
		
		if(valueColour.isEmpty()) {
			DataReader.fillValueColour();
		}
		
		if(valueSymbol.isEmpty()) {
			DataReader.fillValueSymbol();
		}
		
		Point stationPoint = Point.fromLngLat(coord[0], coord[1]);
		Geometry stationGeo = (Geometry)stationPoint;
		Feature stationFeature = Feature.fromGeometry(stationGeo);
		
		stationFeature.addStringProperty("marker-size", "medium");
		stationFeature.addStringProperty("location", nearestSt.getLocation());
		
		
		if(nearestSt.getBattery() < 10) {
			stationFeature.addStringProperty("rgb-string", "#000000");
			stationFeature.addStringProperty("marker-color", "#000000");
			stationFeature.addStringProperty("marker-symbol", "cross");
		}
		
		else {
			
	        String foundColour = "";
	        for(var key : valueColour.keySet()) {
	        	
	        	if((double)key <= Double.parseDouble(nearestSt.getReading())) {
//	        		System.out.println("Reading: " + nearestSt.getReading());
//	        		System.out.println("Key: " + key);
	        		foundColour = valueColour.get(key);
	        		
	        	}
	        }	
	        	        
	        String foundSymbol = "";
	        for(var key : valueSymbol.keySet()) {
	        	
	        	if((double)key <= Double.parseDouble(nearestSt.getReading())) {
	        		
	        		foundSymbol = valueSymbol.get(key);
	        		
	        	}
	        
	        }
	        
	        stationFeature.addStringProperty("rgb-string", foundColour);
	        stationFeature.addStringProperty("marker-color", foundColour);
	        stationFeature.addStringProperty("marker-symbol", foundSymbol);
			
		}
		
		return stationFeature;
		
	}
	
	// This function is mostly used for testing purposes - to visualise the whole map
	public static void createMapWithStations(double[] nWest, double[] nEast, double[] sEast, double[] sWest, HttpClient client, ArrayList<Station> stationList) throws IOException, InterruptedException {
		
		var featureList = new ArrayList<Feature>();
		
		Point confinementNW = Point.fromLngLat(nWest[0], nWest[1]);
		Point confinementNE = Point.fromLngLat(nEast[0], nEast[1]);
		Point confinementSE = Point.fromLngLat(sEast[0], sEast[1]);
		Point confinementSW = Point.fromLngLat(sWest[0], sWest[1]);
		
		LineString confinement = LineString.fromLngLats(Arrays.asList(confinementNW, confinementNE, confinementSE, confinementSW, confinementNW));
		
		Geometry boundaries = (Geometry)confinement;
		
		Feature droneBoundaries = Feature.fromGeometry(boundaries);
		
		featureList.add(droneBoundaries);
		
		for(var station : stationList) {
			
			var locationSplit = station.getLocation().split("\\.");
			var requestLocation = HttpRequest.newBuilder().uri(URI.create("http://localhost:80/words/" + locationSplit[0] + 
					"/" + locationSplit[1] + "/" + locationSplit[2] +"/details.json")).build();
			
			HttpResponse<String> responseStation;
	    	try {
	    		
				responseStation = client.send(requestLocation, BodyHandlers.ofString());
				var result = responseStation.body();
				
				var locationDetails = new Gson().fromJson(result, LocationDetails.class);
				
				var lng = Double.parseDouble(locationDetails.getCoord().getLng());
				var lat = Double.parseDouble(locationDetails.getCoord().getLat());
				
				double[] coord = {lng, lat};
				
				var stationFeature = DataReader.readStation(coord, station);
//				Point stationPoint = Point.fromLngLat(lng, lat);
//				Geometry stationGeo = (Geometry)stationPoint;
//				Feature stationFeature = Feature.fromGeometry(stationGeo);
//				
//				stationFeature.addStringProperty("marker-size", "medium");
//				stationFeature.addStringProperty("location", station.getLocation());
//				stationFeature.addStringProperty("rgb-string", "#aaaaaa");
//				stationFeature.addStringProperty("marker-color", "#aaaaaa");
				
				featureList.add(stationFeature);
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(1);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	
		}
		
        FeatureCollection fc = FeatureCollection.fromFeatures(featureList);
        
        String output = fc.toJson();
        
        PrintWriter out = new PrintWriter("test.geojson");
        out.println(output);
        out.close();
	}
	
	
	// Reads all stations on the current date
	public static ArrayList<Station> readStationsForDay(String day, String month, String year, HttpClient client) {
		
    	var request = HttpRequest.newBuilder()
    	.uri(URI.create("http://localhost:80/maps/" + year + "/" + month + "/" + day + "/air-quality-data.json"))
    	.build();
		
    	ArrayList<Station> stationList = null;
    	
    	// The response object is of class HttpResponse<String>
    	HttpResponse<String> response;
		try {
			
			response = client.send(request, BodyHandlers.ofString());
	    	String result = response.body();
	    	
	    	Type listType = new TypeToken<ArrayList<Station>>() {}.getType();
	    	stationList = new Gson().fromJson(result, listType);
	    	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Unable to connect to server");
			System.exit(1);
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
    	
    	
		return stationList;
		
	}
	
	// This function will create a hashmap that will be used when measuring the stations
	public static HashMap<double[], Station> computeCoordinates(ArrayList<Station> stationList, HttpClient client) {
		
		var mapping = new HashMap<double[], Station>();
		
		for(var station : stationList) {
			
			var locationSplit = station.getLocation().split("\\.");
			var requestLocation = HttpRequest.newBuilder().uri(URI.create("http://localhost:80/words/" + locationSplit[0] + 
					"/" + locationSplit[1] + "/" + locationSplit[2] +"/details.json")).build();
			
			HttpResponse<String> responseStation;
	    	try {
	    		
				responseStation = client.send(requestLocation, BodyHandlers.ofString());
				var result = responseStation.body();
				
				var locationDetails = new Gson().fromJson(result, LocationDetails.class);
				
				var lng = Double.parseDouble(locationDetails.getCoord().getLng());
				var lat = Double.parseDouble(locationDetails.getCoord().getLat());
				
//				System.out.println("Lng: " + lng);
//				System.out.println("Lat: " + lat);
//				System.out.println(station.getLocation());
				
				double[] coord = {lng, lat};
				
				mapping.put(coord, station);
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.out.println("Unable to connect to server");
				System.exit(1);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return mapping;
		
	}
}
