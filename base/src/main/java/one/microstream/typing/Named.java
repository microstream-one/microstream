package one.microstream.typing;

import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.collections.types.XGettingCollection;


/**
 * 
 *
 */
public interface Named
{
	public String name();
	
	
	
	public static <C extends Consumer<? super String>> C toNames(
		final Iterable<? extends Named> items    ,
		final C                         collector
	)
	{
		for(final Named named : items)
		{
			collector.accept(named.name());
		}
		
		return collector;
	}
	
	public static XGettingCollection<String> toNames(final Iterable<? extends Named> items)
	{
		return toNames(items, X.List());
	}
	
}
