package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.lang.Math;

public class PathAlgorithm {

	
	public static LinkedHashMap<double[], Station> createRoute(double[] startCoordinates, HashMap<double[], Station> coordinatesWithStations) {
		
		//We first create an ArrayList to store the coordinates - this gives us simpler access to the individual components of each coordinate
		var coordList = new ArrayList<double[]>();
		coordList.add(startCoordinates);
		
		for(var key : coordinatesWithStations.keySet()) {
			coordList.add(key);
		}
		
		// This is the function that finds the path
		var result = PathAlgorithm.pathFinderAlgorithm(coordinatesWithStations, coordList);
		
		// Create a variable to store the new route
		var route = new LinkedHashMap<double[], Station>();
		
		System.out.println();
		
		// Put the values in it
		for(var i = 1; i < result.size(); i++) {
			
			route.put(result.get(i), coordinatesWithStations.get(result.get(i)));
			
		}
		
		return route;
		
	}
	
	// I've decided to first find a path using the nearest neighbour approach which is a greedy algorithm and then optimize it with 2opt
	private static ArrayList<double[]> pathFinderAlgorithm(HashMap<double[], Station> coordinatesWithStations, ArrayList<double[]> coordList) {
		
		// This temporary array will be used to keep track of the available stations - I want it to be a deep copy of the array
		var temp = new ArrayList<double[]>();
		
		for(var coord : coordList) {
			temp.add(coord);
		}
		
		// The final coordinates of the route will be here
		var result = new ArrayList<double[]>();
		result.add(temp.get(0));
		temp.remove(0);
		
		// Get the starting position
		var currCoord = result.get(0);
		var counter = 0;
		
		
		// First, the greedy solution
		while(counter < coordList.size() - 1) {
			
			var minDist = CalculationFunctions.calculateDistBetweenPoints(currCoord, temp.get(0));
			var nextItem = temp.get(0);
			double distT;
			
			for(var t : temp) {
				
				distT = CalculationFunctions.calculateDistBetweenPoints(currCoord, t);
				if(minDist > distT) {
					
					minDist = distT;
					nextItem = t;
					
				}
			}
			result.add(nextItem);
			temp.remove(nextItem);
			currCoord = nextItem;
			counter += 1;
			
		}

		// After finding the greedy solution, we will optimize it here using Two Opt
		Boolean swap = true;
		while(swap) {
			
			swap = false;
			var bestDistance = PathAlgorithm.calculateTourValue(result);
            for(var i = 1; i < result.size() - 2; i++) {
                for(var j = i + 1; j < result.size() - 1; j++) {
       
                	var newRoute = PathAlgorithm.swap(result, i, j);
                	var newDist = PathAlgorithm.calculateTourValue(newRoute);
                	
                    if(newDist < bestDistance) {
                    	
                    	result = newRoute;
                    	bestDistance = newDist;
                    	swap = true;
                    	
                    }
            	}
        	}
			
		}
		
		for(var i = 0; i < result.size(); i++) {
			
			System.out.println("Result " + i + " lng = " + result.get(i)[0]);
			System.out.println("Result " + i + " lat = " + result.get(i)[1]);
			
		}
		
		System.out.println();
		
		
		// Calculate the total tour value
//		System.out.println(PathAlgorithm.calculateTourValue(result));
		
		System.out.println("Total length: " + PathAlgorithm.calculateTourValue2(result));
		
		
		return result;
	}
	
	
	// Calculate the total length of the given tour
	private static double calculateTourValue(ArrayList<double[]> tourCoordinates) {
		
		double dist = 0;
		
		for(var i = 0; i < tourCoordinates.size(); i++) {
			
			dist += CalculationFunctions.calculateDistBetweenPoints(tourCoordinates.get(i), tourCoordinates.get((i+1) % tourCoordinates.size()));
			
		}
		
		return dist;
	}
	
	private static double calculateTourValue2(ArrayList<double[]> tourCoordinates) {
		
		double dist = 0;
		int movesSum = 0;
		
		System.out.println("First coord lng: " + tourCoordinates.get(0)[0]);
		System.out.println("First coord lat: " + tourCoordinates.get(0)[1]);
		
		System.out.println("Second coord lng: " + tourCoordinates.get(1)[0]);
		System.out.println("Second coord lat: " + tourCoordinates.get(1)[1]);
		
		
		for(var i = 0; i < tourCoordinates.size(); i++) {
			
			var r = CalculationFunctions.calculateDistBetweenPoints(tourCoordinates.get(i), tourCoordinates.get((i+1) % tourCoordinates.size()));
			System.out.println("Distance between station " + i + " and station " + (i+1) % tourCoordinates.size() + " is " + r);
			movesSum += (int)(r/0.0003);
			System.out.println("Average turns to next: " + (int)(r/0.0003));
			dist += r;
			
		}
		
		System.out.println("Average number of total moves: " + movesSum);
		
		return dist;
	}
	
	
	
	// Swap function for the Two Opt solution
	private static ArrayList<double[]> swap(ArrayList<double[]> route, int i, int j) {
		
        ArrayList<double[]> newRoute = new ArrayList<>();

        // Take the array from the beginning to i and add it to the new route
        for(int c = 0; c <= i - 1; c++) {
        
        	newRoute.add(route.get(c));
        
        }

        // Invert order between i and j and add it to the new route
        int q = 0;
        for(int c = i; c <= j; c++) {
        
        	newRoute.add(route.get(j - q));
            q++;
        
        }

        // Add the rest from j to the end
        for(int c = j + 1; c < route.size(); c++) {
           
        	newRoute.add(route.get(c));
        
        }

        return newRoute;
		
	}
	
}
