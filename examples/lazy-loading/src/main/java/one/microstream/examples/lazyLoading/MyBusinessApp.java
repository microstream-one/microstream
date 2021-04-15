
package one.microstream.examples.lazyLoading;

import java.util.HashMap;


public class MyBusinessApp
{
	private final HashMap<Integer, BusinessYear> businessYears = new HashMap<>();
	
	public MyBusinessApp()
	{
		super();
	}
	
	public HashMap<Integer, BusinessYear> getBusinessYears()
	{
		return this.businessYears;
	}
}
