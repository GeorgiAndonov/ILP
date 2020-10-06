package uk.ac.ed.inf.heatmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

public class HelperFunctions {
	
	public static void createMap(double[] startCoord, double[] endCoordLong, double[] endCoordLat, int[][] colourVals) throws IOException {
		
		// We divide by 10 here as we have fixed number of small polygons on each row -> 10.
		// This way we find the difference in coordinates between the small polygons.
    	double longDiff = Math.abs(startCoord[0] - endCoordLong[0]) / 10;
    	double latDiff = Math.abs(startCoord[1] - endCoordLat[1]) / 10;
    	
    	// This will be the list where we will store all the polygons - I've created it this way as
    	// Arrays.asList(x) does not have the add method
        List<Feature> featureList = new ArrayList<>();
        
        // Start values
        double longitude = startCoord[0];
        double latitude = startCoord[1];
        
        // Filling the List with appropriate polygons and their features
        for(var i = 0; i < 10; ++i) {
        	
	        for(var j = 0; j < 10; ++j) {
	        	
	        	// Get the 4 points for the polygon
	            Point firstPoint = Point.fromLngLat(longitude, latitude);
	            Point secondPoint = Point.fromLngLat(longitude + longDiff, latitude);
	            Point thirdPoint = Point.fromLngLat(longitude + longDiff, latitude - latDiff);
	            Point forthPoint = Point.fromLngLat(longitude, latitude - latDiff);
	            
	            // Create the polygon and the feature from it
	            List<List<Point>> newList = Arrays.asList(Arrays.asList(firstPoint, secondPoint, thirdPoint, forthPoint, firstPoint));
	            Polygon polyNew = Polygon.fromLngLats(newList);
	            Geometry gNew = (Geometry)polyNew;
	            Feature fNew = Feature.fromGeometry(gNew);
	            
	            // Add the specified properties and add the feature to the feature list
	            HelperFunctions.addProperties(colourVals[i][j], fNew);
	            featureList.add(fNew);
	            
	            longitude += longDiff;
	        }
        	
	        // Set the coordinates for the next row
	        latitude -= latDiff;
	        longitude = startCoord[0];
	 
        }

        FeatureCollection fc = FeatureCollection.fromFeatures(featureList);
     
        String output = fc.toJson();
        
        PrintWriter out = new PrintWriter("heatmap.geojson");
        out.println(output);
        out.close();
		
	}
	
	public static int[][] readFromFile(String fileName, int ROWS, int COLUMNS) throws FileNotFoundException {
		
		File openPredictions = new File(fileName);
		Scanner scanPredictions = new Scanner(openPredictions); 
		
		var valueMatrix = new int[ROWS][COLUMNS];
		var columnCounter = 0; // This is a helper variable that will track the columns
		
		while(scanPredictions.hasNextLine()) {
			
			var readLine = scanPredictions.nextLine();
			var temp = readLine.split(",");
			
			for(var i = 0; i < temp.length; ++i) {
				
				temp[i] = temp[i].strip();
				
			}
			
			for(var i = 0; i < ROWS; ++i) {
				
				valueMatrix[columnCounter][i] = Integer.parseInt(temp[i]);
				
			}
			
			columnCounter += 1;
		}
		
		return valueMatrix;
		
	}
	
	public static void addProperties(int colourValue, Feature f) {
		
		// First, add the fill-opacity property as it is the same for evey feature
        f.addNumberProperty("fill-opacity", 0.75);
		
        // Create a hashmap to store the intervals
        var propertyValues = new HashMap<Integer, String>();
        propertyValues.put(0, "#00ff00");
        propertyValues.put(32, "#40ff00");
        propertyValues.put(64, "#80ff00");
        propertyValues.put(96, "#c0ff00");
        propertyValues.put(128, "#ffc000");
        propertyValues.put(160, "#ff8000");
        propertyValues.put(192, "#ff4000");
        propertyValues.put(224, "#ff0000");
        
        // Iterate over the map to find the appropriate one
        String foundProperty = "";
        for(Integer key : propertyValues.keySet()) {
        	
        	if(key <= colourValue) {
        		
        		foundProperty = propertyValues.get(key);
        		
        	}
        
        }
        
        // Add the found property
        f.addStringProperty("fill", foundProperty);
        f.addStringProperty("rgb-string", foundProperty);
	}
}
