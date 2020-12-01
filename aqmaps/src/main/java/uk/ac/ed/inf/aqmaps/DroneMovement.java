package uk.ac.ed.inf.aqmaps;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

public class DroneMovement {
	
	
	public static void test() throws FileNotFoundException {
		
		double[] coordinates1 = {-3.188396, 55.944425};
		double[] coordinates2 = {-3.188271, 55.944063};
		
		System.out.println("Test distance: " + CalculationFunctions.calculateDistBetweenPoints(coordinates1, coordinates2));
		
		Point stationPoint1 = Point.fromLngLat(coordinates1[0], coordinates1[1]);
		Geometry stationGeo1 = (Geometry)stationPoint1;
		Feature stationFeature1 = Feature.fromGeometry(stationGeo1);
		
		stationFeature1.addStringProperty("marker-size", "medium");
		stationFeature1.addStringProperty("location", "starting");
		stationFeature1.addStringProperty("rgb-string", "#aaaaaa");
		stationFeature1.addStringProperty("marker-color", "#aaaaaa");
		
		Point stationPoint2 = Point.fromLngLat(coordinates2[0], coordinates2[1]);
		Geometry stationGeo2 = (Geometry)stationPoint2;
		Feature stationFeature2 = Feature.fromGeometry(stationGeo2);
		
		stationFeature2.addStringProperty("marker-size", "medium");
		stationFeature2.addStringProperty("location", "scare.cubs.resort");
		stationFeature2.addStringProperty("rgb-string", "#aaaaaa");
		stationFeature2.addStringProperty("marker-color", "#aaaaaa");
		
		
		var angle = CalculationFunctions.getAngle(coordinates1, coordinates2);
		
		System.out.println("Angle after the first calculation:");
		System.out.println(angle);
		
		var lngNew = coordinates1[0] + Math.cos((angle*Math.PI) / 180)*0.0003;
		var latNew = coordinates1[1] + Math.sin((angle*Math.PI) / 180)*0.0003;
		
		double[] next = {lngNew, latNew};
		
		System.out.println("The distance travelled is: " + CalculationFunctions.calculateDistBetweenPoints(coordinates1, next));
		
		Point nextPoint = Point.fromLngLat(lngNew, latNew);
		
		var lineList = new ArrayList<Point>();
		lineList.add(stationPoint1);
		lineList.add(nextPoint);
		
		
		var dist = CalculationFunctions.calculateDistBetweenPoints(next, coordinates2);
		var featureList = new ArrayList<Feature>();
		var count = 1;

		while(dist > 0.0002) {
			
			angle = CalculationFunctions.getAngle(next, coordinates2);
			System.out.println(angle);
			
			lngNew = next[0] + Math.cos((angle*Math.PI) / 180)*0.0003;
			latNew = next[1] + Math.sin((angle*Math.PI) / 180)*0.0003;
			
			next[0] = lngNew;
			next[1] = latNew;
			
			
			nextPoint = Point.fromLngLat(lngNew, latNew);
			lineList.add(nextPoint);
			
			dist = CalculationFunctions.calculateDistBetweenPoints(next, coordinates2);
			count++;
			
		}
		
		LineString line = LineString.fromLngLats(lineList);
		Geometry lineGeo = (Geometry)line;
		Feature lineFeature = Feature.fromGeometry(lineGeo);
		
		featureList.add(stationFeature1);
		featureList.add(stationFeature2);
		featureList.add(lineFeature);
		
        FeatureCollection fc = FeatureCollection.fromFeatures(featureList);
        
        String output = fc.toJson();
        
        PrintWriter out = new PrintWriter("test2.geojson");
        out.println(output);
        out.close();
        
	}
	
	
	public static void droneMovement(double[] startingCoordinates, HashMap<double[], Station> route, String day, String month, String year) throws FileNotFoundException {
		
		// This variable will count my moves. If the moves get to 150, the program will be terminated and the drone will stop
		int moveCounter = 0;
		
		// This variable will be used to track if we have available stations from the route to reach
		var temp = new ArrayList<double[]>();
		var lineList = new ArrayList<Point>();
		
		// Variable to store the features
		var featureList = new ArrayList<Feature>();
		
		// Fill my temporary array
		for(var key : route.keySet()) {
			temp.add(key);
		}
		
		
		// This will be the beginning of the loop
		double[] currCoord = {startingCoordinates[0], startingCoordinates[1]};
		double[] prevCoord = {0.0, 0.0};
		var nextCoord = temp.get(0);
		var dist = 0.0;
		var angle = 0;
		var counter = 0;
		var routeSize = temp.size();
		var logString = "";

		System.out.println("Route size = " + routeSize);
		
		while(counter < routeSize) {
			
			dist = CalculationFunctions.calculateDistBetweenPoints(currCoord, nextCoord);
			
			// We should always have one initial move
			var initMove = 0;
			while(dist > 0.0002 || initMove == 0) {
				
				angle = CalculationFunctions.getAngle(currCoord, nextCoord);
				moveCounter++;
				
				var lngNew = currCoord[0] + Math.cos((angle*Math.PI) / 180)*0.0003;
				var latNew = currCoord[1] + Math.sin((angle*Math.PI) / 180)*0.0003;
				
				prevCoord[0] = currCoord[0];
				prevCoord[1] = currCoord[1];
				
				currCoord[0] = lngNew;
				currCoord[1] = latNew;
				
				
				var nextPoint = Point.fromLngLat(lngNew, latNew);
				lineList.add(nextPoint);
				
				initMove++;
				dist = CalculationFunctions.calculateDistBetweenPoints(currCoord, nextCoord);
				
				if(dist < 0.0002) {
					
					logString += moveCounter + "," + prevCoord[0] + "," + prevCoord[1] + "," + angle + "," + currCoord[0] + "," + currCoord[1] + "," + route.get(nextCoord).getLocation() + "\n";
					
				} else {
					
					logString += moveCounter + "," + prevCoord[0] + "," + prevCoord[1] + "," + angle + "," + currCoord[0] + "," + currCoord[1] + "," + "null" + "\n";
					
				}
				
				
			}
			
			
//			do {
//				
//				angle = CalculationFunctions.getAngle(currCoord, nextCoord);
//				
//				var lngNew = currCoord[0] + Math.cos((angle*Math.PI) / 180)*0.0003;
//				var latNew = currCoord[1] + Math.sin((angle*Math.PI) / 180)*0.0003;
//				
//				currCoord[0] = lngNew;
//				currCoord[1] = latNew;
//								
//				var nextPoint = Point.fromLngLat(lngNew, latNew);
//				lineList.add(nextPoint);
//				
//				moveCounter++;
//				dist = CalculationFunctions.calculateDistBetweenPoints(currCoord, nextCoord);
//				
//			}while(dist > 0.0002);
			
			System.out.println("Move counter: " + moveCounter);
			
			var nextStation = DataReader.readStation(nextCoord, route.get(nextCoord));
			featureList.add(nextStation);
			counter++;

			if(counter < routeSize) {
				temp.remove(0);
				nextCoord = temp.get(0);	
			}
			
		}
		
		currCoord[0] = nextCoord[0];
		currCoord[1] = nextCoord[1];
		
		System.out.println("Curr coord lng = " + currCoord[0] + " " + "lat = " + currCoord[1]);
		
		nextCoord[0] = startingCoordinates[0];
		nextCoord[1] = startingCoordinates[1];
		
		System.out.println("Next coord lng = " + startingCoordinates[0] + " " + "lat = " + startingCoordinates[1]);
		System.out.println("Next coord lng = " + nextCoord[0] + " " + "lat = " + nextCoord[1]);
		
		dist = CalculationFunctions.calculateDistBetweenPoints(currCoord, nextCoord);
		System.out.println("Distance between last visited and start = " + dist);
		
		while(dist > 0.0003) {
			
			angle = CalculationFunctions.getAngle(currCoord, nextCoord);
			
			var lngNew = currCoord[0] + Math.cos((angle*Math.PI) / 180)*0.0003;
			var latNew = currCoord[1] + Math.sin((angle*Math.PI) / 180)*0.0003;
			
			prevCoord[0] = currCoord[0];
			prevCoord[1] = currCoord[1];
			
			currCoord[0] = lngNew;
			currCoord[1] = latNew;
							
			var nextPoint = Point.fromLngLat(lngNew, latNew);
			lineList.add(nextPoint);
			
			moveCounter++;
			dist = CalculationFunctions.calculateDistBetweenPoints(currCoord, nextCoord);
			
			if(dist > 0.0003) {
				
				logString += moveCounter + "," + prevCoord[0] + "," + prevCoord[1] + "," + angle + "," + currCoord[0] + "," + currCoord[1] + "," + "null" + "\n";
				
			} else {
				
				logString += moveCounter + "," + prevCoord[0] + "," + prevCoord[1] + "," + angle + "," + currCoord[0] + "," + currCoord[1] + "," + "null";
				
			}
			
		}
		
		LineString line = LineString.fromLngLats(lineList);
		Geometry lineGeo = (Geometry)line;
		Feature lineFeature = Feature.fromGeometry(lineGeo);
		
//		System.out.println("Is temp empty? " + temp.isEmpty());
//		if(!temp.isEmpty()) {
//			
//			
//			for(var coord : temp) {
//				
//				Point stationPoint = Point.fromLngLat(coord[0], coord[1]);
//				Geometry stationGeo = (Geometry)stationPoint;
//				Feature stationFeature = Feature.fromGeometry(stationGeo);
//				
//				stationFeature.addStringProperty("marker-size", "medium");
//				stationFeature.addStringProperty("location", route.get(coord).getLocation());
//				stationFeature.addStringProperty("rgb-string", "#aaaaaa");
//				stationFeature.addStringProperty("marker-color", "#aaaaaa");
//				
//				featureList.add(stationFeature);
//				
//			}
//			
//		}
		
		featureList.add(lineFeature);
        FeatureCollection fc = FeatureCollection.fromFeatures(featureList);
        
        String output = fc.toJson();
        
        PrintWriter out = new PrintWriter("readings-" + day + "-" + month + "-" + year + ".geojson");
        out.println(output);
        out.close();
        
        PrintWriter out2 = new PrintWriter("flightpath-" + day + "-" + month + "-" + year + ".txt");
        out2.println(logString);
        out2.close();
        
        System.out.println(moveCounter);
		
	}
	
	

}
