
package one.microstream.examples.customlegacytypehandler;

public class Location
{
	String directions;
	double latitude;
	double longitude;
	
	public Location(final String directions, final double latitude, final double longitude)
	{
		super();
		this.directions = directions;
		this.latitude   = latitude;
		this.longitude  = longitude;
	}
	
	@Override
	public String toString()
	{
		return "Latitude: " + this.latitude + "\nLogitude: " + this.longitude + "\ndirections: " + this.directions;
	}
}
