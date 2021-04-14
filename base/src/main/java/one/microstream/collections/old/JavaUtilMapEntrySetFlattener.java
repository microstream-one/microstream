package one.microstream.collections.old;

import static one.microstream.X.notNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The term "serializer" is already taken (conversion to a byte sequence), as is "sequencer",
 * so this thing will be a "flattener" (order references in a non-trivial data structure in a trivial, flat, sequence).
 * 
 * 
 * 
 *
 */
public class JavaUtilMapEntrySetFlattener<T, K extends T, V extends T> implements Iterator<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T, K extends T, V extends T> JavaUtilMapEntrySetFlattener<T, K, V> New(
		final Iterator<Map.Entry<K, V>> iterator
	)
	{
		return new JavaUtilMapEntrySetFlattener<>(
			notNull(iterator)
		);
	}
	
	public static <T, K extends T, V extends T> JavaUtilMapEntrySetFlattener<T, K, V> New(
		final Map<K, V> map
	)
	{
		return New(
			map.entrySet().iterator()
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Iterator<Map.Entry<K, V>> iterator;

	private Map.Entry<K, V> entry;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected JavaUtilMapEntrySetFlattener(final Iterator<Entry<K, V>> iterator)
	{
		super();
		this.iterator = iterator;
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public boolean hasNext()
	{
		return this.entry == null
			? this.iterator.hasNext()
			: true
		;
	}
	
	@Override
	public T next()
	{
		if(this.entry == null)
		{
			this.entry = this.iterator.next();
			return this.entry.getKey();
		}
		
		final T value = this.entry.getValue();
		this.entry = null;
		return value;
	}
	
}
