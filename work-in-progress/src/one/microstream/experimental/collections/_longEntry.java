package one.microstream.experimental.collections;

import one.microstream.typing._longKeyValue;

public class _longEntry implements _longKeyValue
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final    long key;
	final    int  hash;
	volatile long value;
	volatile _longEntry link;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	_longEntry(final long key, final long value)
	{
		super();
		this.key   = key;
		this.hash  = (int)(key ^ key >>> 32);
		this.value = value;
		this.link  = null;
	}

	_longEntry(final long key, final long value, final _longEntry link)
	{
		super();
		this.key   = key;
		this.hash  = (int)(key ^ key >>> 32);
		this.value = value;
		this.link  = link;
	}



	///////////////////////////////////////////////////////////////////////////
	// setters //
	////////////

	_longEntry setLink(final _longEntry link)
	{
		this.link = link;
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public long key()
	{
		return this.key;
	}

	@Override
	public long value()
	{
		return this.value;
	}

}
