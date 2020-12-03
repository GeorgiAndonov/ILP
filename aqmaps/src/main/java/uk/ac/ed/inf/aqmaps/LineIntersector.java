package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.util.ArrayList;

public class LineIntersector {

	
	public Line2D test;
	public Line2D move;
	
	// Returns true if the line crosses one or more of the lines in the no fly zone - used in the movement function
	public Boolean intersects(ArrayList<ArrayList<Line2D>> noFlyZone) {
		
		for(var poly : noFlyZone) {
			
			for(var line : poly) {
				
				if(this.move.intersectsLine(line)) {
					
					this.test = line;
					return true;
					
				}
				
			}
			
		}
		
		return false;
		
	}
	
	public void setMove(double[] p1, double[] p2) {
		
		this.move = new Line2D.Double(p1[0], p1[1], p2[0], p2[1]);
		
	}
	
	public void setMove2(Line2D l) {
		this.move = l;
	}
	
}
