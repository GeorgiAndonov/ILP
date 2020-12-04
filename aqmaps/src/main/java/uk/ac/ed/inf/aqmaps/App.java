package uk.ac.ed.inf.aqmaps;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.HashMap;




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
     
    	// Read from the server the station list for the day
    	var stationList = DataReader.readStationsForDay(args[0], args[1], args[2], client);
    	// Read from the server the no-fly-zone
    	var noFlyZone = DataReader.getNoFlyArea(client);
    	
    	// Create the confinement area
		var confinementArea = new ArrayList<Line2D>();
		confinementArea.add(new Line2D.Double(NORTHWEST[0], NORTHWEST[1], NORTHEAST[0], NORTHEAST[1]));
		confinementArea.add(new Line2D.Double(NORTHEAST[0], NORTHEAST[1], SOUTHEAST[0], SOUTHEAST[1]));
		confinementArea.add(new Line2D.Double(SOUTHEAST[0], SOUTHEAST[1], SOUTHWEST[0], SOUTHWEST[1]));
		confinementArea.add(new Line2D.Double(SOUTHWEST[0], SOUTHWEST[1], NORTHWEST[0], NORTHWEST[1]));
    	
    	// Get the stations combined with their coordinates
    	HashMap<double[], Station> coordinatesStations = DataReader.computeCoordinates(stationList, client);
    	
    	// Parse the starting coordinates
    	double[] startCoordinates = {Double.parseDouble(args[4]), Double.parseDouble(args[3])};
    	
    	// Create the route the drone's supposed to follow
    	var route = PathAlgorithm.createRoute(startCoordinates, coordinatesStations);
		
		// Initialise new movement
		var newMovement = new DroneMovement(args[5]);
		// Execute the movement
    	newMovement.droneMovement(confinementArea, startCoordinates, route, noFlyZone, args[0], args[1], args[2]);

    }
}
