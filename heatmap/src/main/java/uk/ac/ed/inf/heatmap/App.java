package uk.ac.ed.inf.heatmap;

import java.io.IOException;

public class App 
{
	
	// Setting up the columns and rows of the matrix. As listed in the CW document and on Piazza,
	// the columns and rows are always going to be 10.
	final static int COLUMNS = 10;
	final static int ROWS = 10;
	
	
	// Confinement area coordinates
	final static double[] NORTHWEST = {-3.192473, 55.946233};
	final static double[] SOUTHWEST = {-3.192473, 55.942617};
	final static double[] NORTHEAST = {-3.184319, 55.946233};
	final static double[] SOUTHEAST = {-3.184319, 55.942617};
	
    public static void main( String[] args ) throws IOException
    {

    	// This function reads from file predictions.txt and returns a 2D array containing the values stored in colourVals
    	var colourVals = HelperFunctions.readFromFile(args[0], ROWS, COLUMNS);

    	// This function creates a file named heatmap.geojson containing the the geojson string form of the result
    	HelperFunctions.createMap(NORTHWEST, NORTHEAST, SOUTHEAST, colourVals);
        
    }
}
