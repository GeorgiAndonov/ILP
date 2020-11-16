package uk.ac.ed.inf.aqmaps;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.reflect.Type;

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
	
	
    public static void main( String[] args )
    {
     
    	
    	// HttpClient assumes that it is a GET request by default.
    	var request = HttpRequest.newBuilder()
    	.uri(URI.create("http://localhost:80/maps/2020/01/02/air-quality-data.json"))
    	.build();
    	
    	// The response object is of class HttpResponse<String>
    	HttpResponse<String> response;
		try {
			
			response = client.send(request, BodyHandlers.ofString());
	    	String result = response.body();
	    	
	    	Type listType = new TypeToken<ArrayList<Station>>() {}.getType();
	    	ArrayList<Station> stationList = new Gson().fromJson(result, listType);
	    	
	    	DataReader.createMapWithStations(NORTHWEST, NORTHEAST, SOUTHEAST, SOUTHWEST, client, stationList);
	    	HashMap<double[], Station> test = DataReader.computeCoordinates(stationList, client);
	    	
	    	double[] startCoordinates = {-3.187305, 55.944492};
	    	
	    	var route = DroneMovement.createRoute(startCoordinates, test);
	    	
	    	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Unable to connect to server");
			System.exit(1);
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
    }
}
