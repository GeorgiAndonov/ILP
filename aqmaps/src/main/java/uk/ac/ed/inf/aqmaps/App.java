package uk.ac.ed.inf.aqmaps;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;;




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
    	
//    	for(var st : stationList) {
//    		
//    		System.out.println(st.getLocation());
//    		
//    	}
    	
    	DataReader.createMapWithStations(NORTHWEST, NORTHEAST, SOUTHEAST, SOUTHWEST, client, stationList);
    	HashMap<double[], Station> test = DataReader.computeCoordinates(stationList, client);
    	
//    	for(var t : test.keySet()) {
//    		System.out.println();
//    		System.out.println(t[0] + " " + t[1] + " ");
//    		System.out.println(test.get(t).getLocation());
//    	}
    	
    	double[] startCoordinates = {Double.parseDouble(args[4]), Double.parseDouble(args[3])};
    	
    	var route = PathAlgorithm.createRoute(startCoordinates, test);
    	
		// Printing the locations found
		for(var r : route.keySet()) {
			
			System.out.println(route.get(r).getLocation());
			
		}
		
    	
//    	DroneMovement.test();
    	DroneMovement.droneMovement(startCoordinates, route, args[0], args[1], args[2]);
    	DataReader.getConfinementArea(client);
    	
    }
}
