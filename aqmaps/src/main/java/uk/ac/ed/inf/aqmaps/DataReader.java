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
	
	
	// This function is mostly used for testing purposes.
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
				
				Point stationPoint = Point.fromLngLat(lng, lat);
				Geometry stationGeo = (Geometry)stationPoint;
				Feature stationFeature = Feature.fromGeometry(stationGeo);
				
				stationFeature.addStringProperty("marker-size", "medium");
				stationFeature.addStringProperty("location", station.getLocation());
				stationFeature.addStringProperty("rgb-string", "#aaaaaa");
				stationFeature.addStringProperty("marker-color", "#aaaaaa");
				
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
				
				double[] coord = {lng, lat};
				
				mapping.put(coord, station);
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(1);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return mapping;
		
	}
}
