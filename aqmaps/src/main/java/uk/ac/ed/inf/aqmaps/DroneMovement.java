package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;


// This class moves the drone according to the restrictions
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
	
	
	public static void droneMovement(double[] startingCoordinates, HashMap<double[], Station> route, ArrayList<ArrayList<Line2D>> noFlyZone, String day, String month, String year, String seed) throws FileNotFoundException {
		
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
		
		
		// Loop variables:
		// Random number that'll be used during movement
		var dirDesider = new Random(Long.parseLong(seed));
		double[] currCoord = {startingCoordinates[0], startingCoordinates[1]};
		// This will store the previous coordinates after calculation - used for testing the confinement area
		double[] prevCoord = {0.0, 0.0};
		var nextCoord = temp.get(0);
		var dist = 0.0;
		var angle = 0;
		// This will keep track of how many stations the drone has read
		var counter = 0;
		var routeSize = temp.size();
		// This will store the string that will be written in the flight path text file
		var logString = "";
		
		// Loop ends if everything's been read or the move counter exceeds the limit
		while(counter < routeSize && moveCounter <= 150) {
			
			dist = CalculationFunctions.calculateDistBetweenPoints(currCoord, nextCoord);
			
			// We should always have one initial move
			var initMove = 0;
			while(dist > 0.0002 || initMove == 0) {
				
				angle = CalculationFunctions.getAngle(currCoord, nextCoord);
				moveCounter++;
				
				// Calculate potential next coordinates
				var lngNew = currCoord[0] + Math.cos((angle*Math.PI) / 180)*0.0003;
				var latNew = currCoord[1] + Math.sin((angle*Math.PI) / 180)*0.0003;
				
				prevCoord[0] = currCoord[0];
				prevCoord[1] = currCoord[1];
				
				currCoord[0] = lngNew;
				currCoord[1] = latNew;
				
				// Test if the next coordinates intersect a no fly polygon - find coordinates that do not 
				Line2D move = new Line2D.Double(prevCoord[0], prevCoord[1], currCoord[0], currCoord[1]);
				
				while(CalculationFunctions.intersects(noFlyZone, move)) {
					
					// var linesIntersected = CalculationFunctions.intersectedLines(noFlyZone, move);
					
					angle = dirDesider.nextInt(36) * 10;
					lngNew = prevCoord[0] + Math.cos((angle*Math.PI) / 180)*0.0003;
					latNew = prevCoord[1] + Math.sin((angle*Math.PI) / 180)*0.0003;
					
					currCoord[0] = lngNew;
					currCoord[1] = latNew;
					
					move = new Line2D.Double(prevCoord[0], prevCoord[1], currCoord[0], currCoord[1]);
				}
				
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
			
			System.out.println("Move counter: " + moveCounter);
			
			var nextStation = DataReader.readStation(nextCoord, route.get(nextCoord));
			featureList.add(nextStation);
			counter++;

			if(counter < routeSize) {
				temp.remove(0);
				nextCoord = temp.get(0);	
			}
			
		}
		
		// Start moving towards the starting point if we have available moves
		if(moveCounter < 150) {
			
			currCoord[0] = nextCoord[0];
			currCoord[1] = nextCoord[1];
			
			System.out.println("Curr coord lng = " + currCoord[0] + " " + "lat = " + currCoord[1]);
			
			nextCoord[0] = startingCoordinates[0];
			nextCoord[1] = startingCoordinates[1];
			
			System.out.println("Next coord lng = " + startingCoordinates[0] + " " + "lat = " + startingCoordinates[1]);
			System.out.println("Next coord lng = " + nextCoord[0] + " " + "lat = " + nextCoord[1]);
			
			dist = CalculationFunctions.calculateDistBetweenPoints(currCoord, nextCoord);
			System.out.println("Distance between last visited and start = " + dist);
			
			while(dist > 0.0003 && moveCounter <= 150) {
				
				angle = CalculationFunctions.getAngle(currCoord, nextCoord);
				
				var lngNew = currCoord[0] + Math.cos((angle*Math.PI) / 180)*0.0003;
				var latNew = currCoord[1] + Math.sin((angle*Math.PI) / 180)*0.0003;
				
				prevCoord[0] = currCoord[0];
				prevCoord[1] = currCoord[1];
				
				currCoord[0] = lngNew;
				currCoord[1] = latNew;
				
				Line2D move = new Line2D.Double(prevCoord[0], prevCoord[1], currCoord[0], currCoord[1]);
				
				while(CalculationFunctions.intersects(noFlyZone, move)) {
					
					// var linesIntersected = CalculationFunctions.intersectedLines(noFlyZone, move);
					angle = dirDesider.nextInt(36) * 10;
					lngNew = prevCoord[0] + Math.cos((angle*Math.PI) / 180)*0.0003;
					latNew = prevCoord[1] + Math.sin((angle*Math.PI) / 180)*0.0003;
					
					currCoord[0] = lngNew;
					currCoord[1] = latNew;
					
					move = new Line2D.Double(prevCoord[0], prevCoord[1], currCoord[0], currCoord[1]);
				}
				
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
			
		}
		
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
		
		
		// Add the LineString to the feature list and finish print to files
		LineString line = LineString.fromLngLats(lineList);
		Geometry lineGeo = (Geometry)line;
		Feature lineFeature = Feature.fromGeometry(lineGeo);
		
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
