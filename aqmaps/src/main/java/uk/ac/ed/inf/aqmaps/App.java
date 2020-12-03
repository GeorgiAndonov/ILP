package uk.ac.ed.inf.aqmaps;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;;




public class App 
{
	
	
	// Confinement area coordinates
	final static double[] NORTHWEST = {-3.192473, 55.946233};
	final static double[] SOUTHWEST = {-3.192473, 55.942617};
	final static double[] NORTHEAST = {-3.184319, 55.946233};
	final static double[] SOUTHEAST = {-3.184319, 55.942617};
	
	
	private static final HttpClient client = HttpClient.newHttpClient();
	
	
    public static void main( String[] args ) throws IOException, InterruptedException
    {
     
    	var stationList = DataReader.readStationsForDay(args[0], args[1], args[2], client);
    	var noFlyZone = DataReader.getNoFlyArea(client);
    	
		var confinementArea = new ArrayList<Line2D>();
		confinementArea.add(new Line2D.Double(NORTHWEST[0], NORTHWEST[1], NORTHEAST[0], NORTHEAST[1]));
		confinementArea.add(new Line2D.Double(NORTHEAST[0], NORTHEAST[1], SOUTHEAST[0], SOUTHEAST[1]));
		confinementArea.add(new Line2D.Double(SOUTHEAST[0], SOUTHEAST[1], SOUTHWEST[0], SOUTHWEST[1]));
		confinementArea.add(new Line2D.Double(SOUTHWEST[0], SOUTHWEST[1], NORTHWEST[0], NORTHWEST[1]));
    	
//    	for(var st : stationList) {
//    		
//    		System.out.println(st.getLocation());
//    		
//    	}
    	
    	DataReader.createMapWithStations(NORTHWEST, NORTHEAST, SOUTHEAST, SOUTHWEST, client, stationList);
    	HashMap<double[], Station> coordinatesStations = DataReader.computeCoordinates(stationList, client);
    	
//    	for(var t : test.keySet()) {
//    		System.out.println();
//    		System.out.println(t[0] + " " + t[1] + " ");
//    		System.out.println(test.get(t).getLocation());
//    	}
    	
    	double[] startCoordinates = {Double.parseDouble(args[4]), Double.parseDouble(args[3])};
    	
    	var route = PathAlgorithm.createRoute(startCoordinates, coordinatesStations);
    	
		// Printing the locations found
		for(var r : route.keySet()) {
			
			System.out.println(route.get(r).getLocation());
			
		}
		
    	
//    	DroneMovement.test();
    	DroneMovement.droneMovement(confinementArea, startCoordinates, route, noFlyZone, args[0], args[1], args[2], args[5]);
//    	DroneMovement.droneMovement2(confinementArea, startCoordinates, route, noFlyZone, args[0], args[1], args[2], args[5]);
//		DataReader.printPolyLineInfo(noFlyZone);
		
//		double[] p1 = {-3.186908, 55.945421};
//		double[] p2 = {-3.187101, 55.945651};
//		
//		System.out.println(CalculationFunctions.getAngle(p1, p2));
//		
//		Point p11 = Point.fromLngLat(p1[0], p1[1]);
//		Point p12 = Point.fromLngLat(p2[0], p2[1]);
//		Point p13 = Point.fromLngLat(-3.186998, 55.945366);
//		Point p14 = Point.fromLngLat(-3.187101, 55.945691);
//		Point p15 = Point.fromLngLat(-3.1872937, 55.945881);
//		
////		LineString ls1 = LineString.fromLngLats(Arrays.asList(p13, p14));
////		Geometry gs1 = (Geometry)ls1;
////		Feature fs1 = Feature.fromGeometry(gs1);
//		
//		LineString ls2 = LineString.fromLngLats(Arrays.asList(p14, p15));
//		Geometry gs2 = (Geometry)ls2;
//		Feature fs2 = Feature.fromGeometry(gs2);
//		
//		LineString ls = LineString.fromLngLats(Arrays.asList(p11, p12));
//		Geometry gs = (Geometry)ls;
//		Feature fs = Feature.fromGeometry(gs);
//		
//		var tt = new ArrayList<Feature>();
//		tt.add(fs);
////		tt.add(fs1);
//		tt.add(fs2);
//		
//		FeatureCollection fc = FeatureCollection.fromFeatures(tt);
//		
//        String output = fc.toJson();
//        
//        PrintWriter out = new PrintWriter("testing11.geojson");
//        out.println(output);
//        out.close();
		
    }
}
