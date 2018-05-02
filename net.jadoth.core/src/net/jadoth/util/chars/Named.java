package net.jadoth.util.chars;

import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingCollection;


/**
 * @author Thomas Muenz
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
