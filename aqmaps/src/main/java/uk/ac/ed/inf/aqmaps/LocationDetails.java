package uk.ac.ed.inf.aqmaps;

public class LocationDetails {

	
	// I've created getters only for the coordinates as they would be the only one that will be used in the calculation
	private String country;
	private Square square;
	private String nearestPlace;
	private Coordinates coordinates;
	private String words;
	private String language;
	private String map;
	
	
	public static class Square {
		
		Coordinates southwest;
		Coordinates northeast;
		
	}
	
	public static class Coordinates {
		
		private String lng;
		private String lat;
		
		public String getLng() {
			return lng;
		}

		public String getLat() {
			return lat;
		}
		
	}

	public Coordinates getCoord() {
		return coordinates;
	}
}
