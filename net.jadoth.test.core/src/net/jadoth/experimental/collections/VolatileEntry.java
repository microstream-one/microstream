package net.jadoth.experimental.collections;

import net.jadoth.collections.KeyValue;

public final class VolatileEntry implements KeyValue<Object, Object>
{
	// do not cache fields on stack
	final Object key;
	volatile Object value;
	volatile VolatileEntry link;


	VolatileEntry(final Object key, final Object value)
	{
		super();
		this.key = key;
		this.value = value;
		this.link = null;
	}

	VolatileEntry(final Object key, final Object value, final VolatileEntry link)
	{
		super();
		this.key = key;
		this.value = value;
		this.link = link;
	}

	VolatileEntry setLink(final VolatileEntry link)
	{
		this.link = link;
		return this;
	}

	@Override
	public Object key()
	{
		return this.key;
	}

	@Override
	public Object value()
	{
		return this.value;
	}

}
