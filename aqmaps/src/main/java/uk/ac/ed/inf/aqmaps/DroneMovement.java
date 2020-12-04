package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
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
	
	public static void droneMovement(ArrayList<Line2D> confinementArea, double[] startingCoordinates, HashMap<double[], Station> route, ArrayList<ArrayList<Line2D>> noFlyZone, String day, String month, String year, String seed) throws FileNotFoundException {
		
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
		
		var lineIntersector = new LineIntersector();
		
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
		
		// Those variables will be used in the intersection coordination
		double[] coordChange1 = {0.0, 0.0};
		double[] coordChange2 = {0.0, 0.0};
		var move = new Line2D.Double(prevCoord[0], prevCoord[1], currCoord[0], currCoord[1]);
		
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
				
				move = new Line2D.Double(prevCoord[0], prevCoord[1], currCoord[0], currCoord[1]);
				// Test if the next coordinates leave the confinement area
				while(lineIntersector.isInConfinement(confinementArea, move)) {
					
					angle = dirDesider.nextInt(36) * 10;
					lngNew = prevCoord[0] + Math.cos((angle*Math.PI) / 180)*0.0003;
					latNew = prevCoord[1] + Math.sin((angle*Math.PI) / 180)*0.0003;
					
					currCoord[0] = lngNew;
					currCoord[1] = latNew;
					
					move = new Line2D.Double(prevCoord[0], prevCoord[1], currCoord[0], currCoord[1]);
					
				}
				
				// Test if the next coordinates intersect a no fly polygon
				while(lineIntersector.intersects(noFlyZone, move)) {
					
					coordChange1[0] = lineIntersector.getIntersected().getX1();
					coordChange1[1] = lineIntersector.getIntersected().getY1();
					
					coordChange2[0] = lineIntersector.getIntersected().getX2();
					coordChange2[1] = lineIntersector.getIntersected().getY2();
					
					angle = CalculationFunctions.getAngle(coordChange1, coordChange2);
					
					// This takes into consideration if we have opposite angles
					angle = (angle + 180) % 360;
					
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
			
			nextCoord[0] = startingCoordinates[0];
			nextCoord[1] = startingCoordinates[1];
			
			dist = CalculationFunctions.calculateDistBetweenPoints(currCoord, nextCoord);
			
			while(dist > 0.0003 && moveCounter <= 150) {
				
				angle = CalculationFunctions.getAngle(currCoord, nextCoord);
				
				var lngNew = currCoord[0] + Math.cos((angle*Math.PI) / 180)*0.0003;
				var latNew = currCoord[1] + Math.sin((angle*Math.PI) / 180)*0.0003;
				
				prevCoord[0] = currCoord[0];
				prevCoord[1] = currCoord[1];
				
				currCoord[0] = lngNew;
				currCoord[1] = latNew;
				
				move = new Line2D.Double(prevCoord[0], prevCoord[1], currCoord[0], currCoord[1]);
				
				// Test if the next coordinates leave the confinement area
				while(lineIntersector.isInConfinement(confinementArea, move)) {
					
					angle = dirDesider.nextInt(36) * 10;
					lngNew = prevCoord[0] + Math.cos((angle*Math.PI) / 180)*0.0003;
					latNew = prevCoord[1] + Math.sin((angle*Math.PI) / 180)*0.0003;
					
					currCoord[0] = lngNew;
					currCoord[1] = latNew;
					
					move = new Line2D.Double(prevCoord[0], prevCoord[1], currCoord[0], currCoord[1]);
					
				}
				
				// Test if the next coordinates intersect a no fly polygon
				while(lineIntersector.intersects(noFlyZone, move)) {
					
					coordChange1[0] = lineIntersector.getIntersected().getX1();
					coordChange1[1] = lineIntersector.getIntersected().getY1();
					
					coordChange2[0] = lineIntersector.getIntersected().getX2();
					coordChange2[1] = lineIntersector.getIntersected().getY2();
					
					angle = CalculationFunctions.getAngle(coordChange1, coordChange2);
					
					// This takes into consideration if we have opposite angles
					angle = (angle + 180) % 360;
					
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
				
				System.out.println("Move counter: " + moveCounter);
				
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
