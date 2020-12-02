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
import java.awt.geom.Line2D;
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
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;;


// This class is a helper class that will help me visualise some of the stations and read the information from the server
public class DataReader {
	
	// This will store the values to colour - I've made it private because I do not want it to be accessed by other classes
	private static HashMap<Integer, String> valueColour = new HashMap<Integer, String>();
	
	// This will store the values to marker symbol - same as the above in terms of privacy
	// Although we have only two options, doing it this way with HashMap would make it easier for adding other symbols in the future.
	private static HashMap<Integer, String> valueSymbol = new HashMap<Integer, String>();
	
	
	// Filling functions when the maps are empty
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
	
	// This function reads the values from a station and returns the appropriate feature for it
	public static Feature readStation(double[] coord, Station nearestSt) {
		
		// Check if the maps are empty and if that's the case - fill them with the necessary information
		if(valueColour.isEmpty()) {
			DataReader.fillValueColour();
		}
		
		if(valueSymbol.isEmpty()) {
			DataReader.fillValueSymbol();
		}
		
		// Create the feature
		Point stationPoint = Point.fromLngLat(coord[0], coord[1]);
		Geometry stationGeo = (Geometry)stationPoint;
		Feature stationFeature = Feature.fromGeometry(stationGeo);
		
		stationFeature.addStringProperty("marker-size", "medium");
		stationFeature.addStringProperty("location", nearestSt.getLocation());
		
		// Check if the station is eligible to be read and read it
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
	
	// This function is mostly used for testing purposes - to visualise the whole map without the path and containing the confinement area
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
	
	
	// Reads all stations on a given date and returns them in a list - check the class Station for more information about the objects
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
	
	// This function will create a hashmap that will be used when measuring the stations - it maps the coordinates to the specified station
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
	
	// This function constructs the list containing the no fly polygons
	public static ArrayList<ArrayList<Line2D>> getConfinementArea(double[] nWest, double[] nEast, double[] sEast, double[] sWest, HttpClient client) {
		
		var polygonList = new ArrayList<ArrayList<Line2D>>();
		
		var requestLocation = HttpRequest.newBuilder().uri(URI.create("http://localhost:80/buildings/no-fly-zones.geojson")).build();
		HttpResponse<String> response;
    	try {
    		
			response = client.send(requestLocation, BodyHandlers.ofString());
			var result = response.body();
			
			var noFlyFeatures = FeatureCollection.fromJson(result);
			var noFlyList = noFlyFeatures.features();
			
			for(var nf : noFlyList) {
				
				// Each polygon is a list of objects of type Line2D
				var polygon = new ArrayList<Line2D>();
				
				System.out.println(nf.getProperty("name"));
				
				// Extract the coordinates from the geojson feature
				var geoNF = nf.geometry();
				var geoPoly = (Polygon)geoNF;
				var geoPolyCoord = geoPoly.coordinates().get(0);
				
				for(var i = 0; i < geoPolyCoord.size() - 1; i++) {
					
//					System.out.println(geoPolyCoord.get(i).coordinates().toString());
					
					// Create a line with the specified coordinates and add it to the polygon
					var l = new Line2D.Double(geoPolyCoord.get(i).longitude(), geoPolyCoord.get(i).latitude(), geoPolyCoord.get(i+1).longitude(), geoPolyCoord.get(i+1).latitude());
					polygon.add(l);
				}
				
				// Add the polygon to the final list
				polygonList.add(polygon);
				
			}
			
			// Finally, we add the confinement area as it is predefined for our project
			var confinementArea = new ArrayList<Line2D>();
			confinementArea.add(new Line2D.Double(nWest[0], nWest[1], nEast[0], nEast[1]));
			confinementArea.add(new Line2D.Double(nEast[0], nEast[1], sEast[0], sEast[1]));
			confinementArea.add(new Line2D.Double(sEast[0], sEast[1], sWest[0], sWest[1]));
			confinementArea.add(new Line2D.Double(sWest[0], sWest[1], nWest[0], nWest[1]));
			
			polygonList.add(confinementArea);
			
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("Unable to connect to server");
			System.exit(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
		return polygonList;
		
	}
	
	
	// This is a test function to test if the no fly zones are created properly - prints some information about them and creates a map that checks
	// if everything is working properly
	public static void printPolyLineInfo(ArrayList<ArrayList<Line2D>> polys) throws FileNotFoundException {
		
		var j = 1;
		
		// Check the information for each line in each polygon
    	for(var poly : polys) {
    		
    		var i = 1;
    		System.out.println("Polygon num " + j + " :");
    		
			for(var line : poly) {
				
				System.out.println("Line " + i + " :");
				System.out.println("Line X1: " + line.getX1());
				System.out.println("Line Y1: " + line.getY1());
				System.out.println("Line X2: " + line.getX2());
				System.out.println("Line Y2: " + line.getY2());
				i++;
			}
			
			j++;
			System.out.println();
    	}
    	
    	var featureList = new ArrayList<Feature>();
    	
    	// Create a geojson object to test the visualisation
    	for(var poly : polys) {
    		
    		var lineList = new ArrayList<Point>();
    		
    		for(var line : poly) {
    			
    			Point l1 = Point.fromLngLat(line.getX1(), line.getY1());
    			Point l2 = Point.fromLngLat(line.getX2(), line.getY2());
    			
    			if(!lineList.contains(l1)) {
    				lineList.add(l1);
    			}
    			
    			lineList.add(l2);
    			
    		}
    		LineString ls = LineString.fromLngLats(lineList);
    		Geometry lineS = (Geometry)ls;
    		Feature lsF = Feature.fromGeometry(lineS);
    		featureList.add(lsF);
    		
    	}
    	
    	// Add a testing line and create the file for testing
    	Point q1 = Point.fromLngLat(-3.1868, 55.9446);
    	Point q2 = Point.fromLngLat(-3.1879, 55.9447);
    	LineString ls2 = LineString.fromLngLats(Arrays.asList(q1, q2));
    	Geometry ls2G = (Geometry)ls2;
    	Feature ls2F = Feature.fromGeometry(ls2G);
    	featureList.add(ls2F);
    	
        FeatureCollection fc = FeatureCollection.fromFeatures(featureList);
        
        String output = fc.toJson();
        
        PrintWriter out = new PrintWriter("testing.geojson");
        out.println(output);
        out.close();
    	
		
	}
	
}
