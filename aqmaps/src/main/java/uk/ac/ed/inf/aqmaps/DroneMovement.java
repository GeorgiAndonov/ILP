package uk.ac.ed.inf.aqmaps;

import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Math;

public class DroneMovement {

	
	public static HashMap<double[], Station> createRoute(double[] startCoordinates, HashMap<double[], Station> coordinatesWithStations) {
		
		//We first create an ArrayList to store the coordinates - this gives us simpler access to the individual components of each coordinate
		var coordList = new ArrayList<double[]>();
		coordList.add(startCoordinates);
		
		for(var key : coordinatesWithStations.keySet()) {
			coordList.add(key);
		}
		
		// This is the function that finds the path
		var route = DroneMovement.pathFinderAlgorithm(coordinatesWithStations, coordList);
		
		return route;
		
	}
	
	// I've decided to first find a path using the nearest neighbour approach which is a greedy algorithm and then optimize it with 2opt
	private static HashMap<double[], Station> pathFinderAlgorithm(HashMap<double[], Station> coordinatesWithStations, ArrayList<double[]> coordList) {
		
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
			
			var minDist = DroneMovement.calculateDistBetweenPoints(currCoord, temp.get(0));
			System.out.println(minDist);
			var nextItem = temp.get(0);
			double distT;
			
			for(var t : temp) {
				
				distT = DroneMovement.calculateDistBetweenPoints(currCoord, t);
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
			var bestDistance = DroneMovement.calculateTourValue(result);
            for(var i = 1; i < result.size() - 2; i++) {
                for(var j = i + 1; j < result.size() - 1; j++) {
       
                	var newRoute = DroneMovement.swap(result, i, j);
                	var newDist = DroneMovement.calculateTourValue(newRoute);
                	
                    if(newDist < bestDistance) {
                    	
                    	result = newRoute;
                    	bestDistance = newDist;
                    	swap = true;
                    	
                    }
            	}
        	}
			
		}
		
		// Create a variable to store the new route
		var route = new HashMap<double[], Station>();
		
		// Put the values in it
		for(var i = 1; i < result.size(); ++i) {
			
			route.put(result.get(i), coordinatesWithStations.get(result.get(i)));
			
		}
		
		// Printing the locations found
		for(var r : route.keySet()) {
			
			System.out.println(route.get(r).getLocation());
			
		}
		
		// Calculate the total tour value
		System.out.println(DroneMovement.calculateTourValue(result));
		
		return route;
	}
	
	
//	private static ArrayList<double[]> TwoOptOptimization(ArrayList<double[]> result) {
//		
//		var optimizedResult = new ArrayList<double[]>();
//		
//		Boolean swap = true;
//		while(swap) {
//			
//			swap = false;
//			var bestDistance = DroneMovement.calculateTourValue(result);
//            for(var i = 1; i < result.size() - 2; i++) {
//                for(var j = i + 1; j < result.size() - 1; j++) {
//       
//                	var newRoute = DroneMovement.swap(result, i, j);
//                	var newDist = DroneMovement.calculateTourValue(newRoute);
//                	
//                    if(newDist < bestDistance) {
//                    	
//                    	optimizedResult = newRoute;
//                    	bestDistance = newDist;
//                    	swap = true;
//                    	
//                    }
//            	}
//        	}
//			
//		}
//		
//		return null;
//	}
	
	
	// Calculate the total length of the given tour
	private static double calculateTourValue(ArrayList<double[]> tourCoordinates) {
		
		double dist = 0;
		
		for(var i = 0; i < tourCoordinates.size(); ++i) {
			
			dist += DroneMovement.calculateDistBetweenPoints(tourCoordinates.get(i), tourCoordinates.get((i+1) % tourCoordinates.size()));
			
		}
		
		return dist;
	}
	
	// Helper function that will help us with calculating the distance between 2 points
	private static double calculateDistBetweenPoints(double[] point1, double[] point2) {
		
		return Math.hypot(Math.abs(point1[0] - point2[0]), Math.abs(point1[1] - point2[1]));
		
	}
	
	
	// Swap function for the Two Opt solution
	private static ArrayList<double[]> swap(ArrayList<double[]> route, int i, int j) {
		
        //conducts a 2 opt swap by inverting the order of the points between i and j
        ArrayList<double[]> newRoute = new ArrayList<>();

        //take array up to first point i and add to newTour
        for(int c = 0; c <= i - 1; c++) {
        
        	newRoute.add(route.get(c));
        
        }

        //invert order between 2 passed points i and j and add to newTour
        int dec = 0;
        for(int c = i; c <= j; c++) {
        
        	newRoute.add(route.get(j - dec));
            dec++;
        
        }

        //append array from point j to end to newTour
        for(int c = j + 1; c < route.size(); c++) {
           
        	newRoute.add(route.get(c));
        
        }

        return newRoute;
		
	}
	
}
