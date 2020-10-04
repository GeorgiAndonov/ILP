package uk.ac.ed.inf.heatmap;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

/**
 * Hello world!
 *
 */
public class App 
{
	
	// Setting up the columns and rows of the matrix. As listed in the CW document and on Piazza,
	// the columns and rows are always going to be 10.
	final static int COLUMNS = 10;
	final static int ROWS = 10;
	
	
	// Confinement area coordinates
	final static double[] NORTHWEST = {55.946233, -3.192473};
	final static double[] SOUTHWEST = {55.942617, -3.192473};
	final static double[] NORTHEAST = {55.946233, -3.184319};
	final static double[] SOUTHEAST = {55.942617, -3.184319};
	
    public static void main( String[] args ) throws IOException
    {
//    	System.out.println(args[0]);
//    	File f = new File(args[0]);
//    	Scanner sc = new Scanner(f);
//    	
//    	var test = sc.nextLine();
//    	System.out.println(test);
//    	
//    	var test2 = test.split(",");
//    	
//    	for(var i = 0; i < test2.length; ++i) {
//    		
//    		System.out.println(test2[i]);
//    		
//    	}
//    	
//    	double c = Double.parseDouble(test2[1]);
//    	System.out.println(c);
//    	
//    	
    	double vertDiff = Math.abs(NORTHWEST[1] - NORTHEAST[1]) / 10;
    	double horDiff = Math.abs(NORTHWEST[0] - SOUTHWEST[0]) / 10;
    	
    	
    	
    	var test = HelperFunctions.readFromFile(args[0], ROWS, COLUMNS);
    	
    	for(var i = 0; i < COLUMNS; ++i) {
    		
    		for(var j = 0; j < ROWS; ++j) {
    			
    			System.out.print(test[i][j] + " ");
    			
    		}
    		
    		System.out.println();
    	}
    	
        System.out.println( "Hello World!" );
        
        Point one = Point.fromLngLat(NORTHWEST[1], NORTHWEST[0]);
        Point two = Point.fromLngLat(SOUTHWEST[1], SOUTHWEST[0]);
        Point three = Point.fromLngLat(NORTHEAST[1], NORTHEAST[0]);
        Point four = Point.fromLngLat(SOUTHEAST[1], SOUTHEAST[0]);
        
        List<Point> ls = Arrays.asList(one, three, four, two, one);
        LineString form = LineString.fromLngLats(ls);
//        List<Point> line1 = Arrays.asList(one, two);
//        List<Point> line2 = Arrays.asList(one, three);
//        List<Point> line3 = Arrays.asList(two, four);
//        List<Point> line4 = Arrays.asList(three, four);
//        
//        List<List<Point>> poly = Arrays.asList(line1, line2, line3, line4);
//        
//        Polygon p = Polygon.fromLngLats(poly);
        Geometry g = (Geometry)form;
        Feature f = Feature.fromGeometry(g);
//        
        List<Feature> fl = new ArrayList<>();
//        fl.add(f);
        
        double longitude = NORTHWEST[1];
        double latitude = NORTHWEST[0];
        for(var i = 0; i < 10; ++i) {
        	
	        for(var j = 0; j < 10; ++j) {
	        	
	            Point first = Point.fromLngLat(longitude, latitude);
	            Point second = Point.fromLngLat(longitude + vertDiff, latitude);
	            Point third = Point.fromLngLat(longitude + vertDiff, latitude - horDiff);
	            Point forth = Point.fromLngLat(longitude, latitude - horDiff);
	            
	            List<Point> lsNew = Arrays.asList(first, second, third, forth, first);
	            LineString formNew = LineString.fromLngLats(lsNew);
	            Geometry gNew = (Geometry)formNew;
	            Feature fNew = Feature.fromGeometry(gNew);
	            fl.add(fNew);
	        	
	            longitude += vertDiff;
	        }
        	
	        latitude = latitude - horDiff;
	        longitude = NORTHWEST[1];
	 
        }

        
        FeatureCollection fc = FeatureCollection.fromFeatures(fl);
     
        String output = fc.toJson();
        
        File out = new File("result.geojson");
        
        FileOutputStream fos = new FileOutputStream(out.getName());
        DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(fos));
        outStream.writeUTF(output);
        outStream.close();
        
    }
}
