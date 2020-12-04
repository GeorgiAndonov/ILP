package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.util.ArrayList;

public class LineIntersector {

	// Stores the line that's been intersected
	private Line2D intersected;
	
	// Returns true if the line crosses one or more of the lines in the no fly zone - used in the movement function
	public Boolean intersects(ArrayList<ArrayList<Line2D>> noFlyZone, Line2D move) {
		
		for(var poly : noFlyZone) {
			
			for(var line : poly) {
				
				if(move.intersectsLine(line)) {
					
					this.intersected = line;
					return true;
					
				}
				
			}
			
		}
		
		return false;
		
	}
	
	
	public Boolean isInConfinement(ArrayList<Line2D> confinementArea, Line2D move) {
		
		for(var line : confinementArea) {

			if(move.intersectsLine(line)) {
				
				return true;
				
			}
			
		}
		
		return false;
	}


	public Line2D getIntersected() {
		return intersected;
	}
	
}
