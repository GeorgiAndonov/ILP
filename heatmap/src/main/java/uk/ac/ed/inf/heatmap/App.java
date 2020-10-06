package uk.ac.ed.inf.heatmap;

import java.io.IOException;

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
	final static double[] NORTHWEST = {-3.192473, 55.946233};
	final static double[] SOUTHWEST = {-3.192473, 55.942617};
	final static double[] NORTHEAST = {-3.184319, 55.946233};
	final static double[] SOUTHEAST = {-3.184319, 55.942617};
	
    public static void main( String[] args ) throws IOException
    {

    	var colourVals = HelperFunctions.readFromFile(args[0], ROWS, COLUMNS);
    	
    	for(var i = 0; i < COLUMNS; ++i) {
    		
    		for(var j = 0; j < ROWS; ++j) {
    			
    			System.out.print(colourVals[i][j] + " ");
    			
    		}
    		
    		System.out.println();
    	}
    	
    	HelperFunctions.createMap(NORTHWEST, NORTHEAST, SOUTHEAST, colourVals);
        
    }
}
