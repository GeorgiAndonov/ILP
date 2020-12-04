package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.util.Random;

public class CalculationFunctions {
	
	// Helper function that calculates the distance between 2 points
	public static double calculateDistBetweenPoints(double[] point1, double[] point2) {
		
		return Math.hypot(Math.abs(point1[0] - point2[0]), Math.abs(point1[1] - point2[1]));
		
	}
	
	// Helper function that will return the angle between 2 points rounded to the nearest 10th
	public static int getAngle(double[] p1, double[] p2) {
		
		var angle = (int)Math.toDegrees(Math.atan2(p2[1] - p1[1], p2[0] - p1[0]));
		
		angle = (int)(Math.round(angle / 10.0) * 10);
		
		if(angle < 0) {
			angle += 360;
		}
		
		if(angle == 360) {
			angle = 0;
		}
		
		return angle;
	}
	
	public static int getAngle(double[] p1, double[] p2, double lastAngle, Random rand) {
		
		var angle = (int)Math.toDegrees(Math.atan2(p2[1] - p1[1], p2[0] - p1[0]));
		
		angle = (int)(Math.round(angle / 10.0) * 10);
		
		if(angle < 0) {
			angle += 360;
		}
		
		if(angle == 360) {
			angle = 0;
		}
		
		if(angle % 180 == lastAngle % 180 && angle != lastAngle) {
			
			angle = rand.nextInt(36) * 10;
			
		}
		
		
		return angle;
	}
	
}
