package one.microstream.examples.customlegacytypehandler;

/**
 * 
 * This file contains to variants of the "NicePlace" class.
 * Both variants of that class contain some code that is only
 * required to avoid source code modifications outside this file to
 * run the legacy type handler example.
 * 
 * It is necessary to do the code switching since java does not like
 * identical named classes in one project.
 * 
 */


/**
 * 
 * This is the first generation of the "NicePlace" class,
 * use this one first to setup a storage with a legacy type
 * 
 */
public class NicePlace
{
	public String name;
	public String directions;

	//This field just exists to avoid compiler issues in the LegacyTypeHandler, don't persist it
	public transient Location location;
	
	public NicePlace()
	{
		super();
	}
	
	public NicePlace(final String name, final String directions)
	{
		super();
		this.name = name;
		this.directions = directions;
	}
	
	@Override
	public String toString()
	{
		return "Nice Place: " + this.name + "\ndirections: " + this.directions;
	}
}

/**
 * 
 * This is the second generation of the "NicePlace" class,
 * use this one to apply the LegacyTypeHandler
 *
 */
//public class NicePlace
//{
//	String name;
//	Location location;
//
//	public NicePlace()
//	{
//		super();
//	}
//
//	public NicePlace(String name, Location location)
//	{
//		super();
//		this.name = name;
//		this.location = location;
//	}
//
//	//This constructor just exists to avoid compiler issues in the LegacyTypeHandler
//	public NicePlace(String name, String locationDescrption)
//	{
//		super();
//		this.name = name;
//	}
//
//	public String toString()
//	{
//		return "Nice Place: " + name + "\n" + location;
//	}
//}
