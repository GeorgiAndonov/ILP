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
    	
    	
    	DataReader.createMapWithStations(NORTHWEST, NORTHEAST, SOUTHEAST, SOUTHWEST, client, stationList);
    	HashMap<double[], Station> coordinatesStations = DataReader.computeCoordinates(stationList, client);
    	
    	double[] startCoordinates = {Double.parseDouble(args[4]), Double.parseDouble(args[3])};
    	
    	var route = PathAlgorithm.createRoute(startCoordinates, coordinatesStations);
    	
		// Printing the locations found
		for(var r : route.keySet()) {
			
			System.out.println(route.get(r).getLocation());
			
		}

    	DroneMovement.droneMovement(confinementArea, startCoordinates, route, noFlyZone, args[0], args[1], args[2], args[5]);

		
    }
}
