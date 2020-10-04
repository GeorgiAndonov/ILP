package uk.ac.ed.inf.heatmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class HelperFunctions {
	
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
	
}
