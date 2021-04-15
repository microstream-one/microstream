
package one.microstream.examples.items;

import java.util.ArrayList;
import java.util.List;


public class DataRoot
{
	private final List<Item> items = new ArrayList<>();
	
	public DataRoot()
	{
		super();
	}
	
	public List<Item> items()
	{
		return this.items;
	}
}
