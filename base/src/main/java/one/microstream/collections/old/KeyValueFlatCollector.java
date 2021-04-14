package one.microstream.collections.old;

import java.util.function.BiConsumer;

import one.microstream.math.XMath;

public final class KeyValueFlatCollector<K, V> implements BiConsumer<K, V>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <K, V> KeyValueFlatCollector<K, V> New(final int keyValueEntryCount)
	{
		// note that an entry count of 0 is valid (e.g. empty map)
		return new KeyValueFlatCollector<>(
			new Object[XMath.notNegative(keyValueEntryCount) * 2]
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Object[] array;
	
	private int i = 0;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	KeyValueFlatCollector(final Object[] array)
	{
		super();
		this.array = array;
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void accept(final K key, final V value)
	{
		this.array[this.i    ] = key  ;
		this.array[this.i + 1] = value;
		this.i += 2;
	}
	
	public final Object[] yield()
	{
		return this.array;
	}
	
}
