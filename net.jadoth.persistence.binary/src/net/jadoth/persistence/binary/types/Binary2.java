package net.jadoth.persistence.binary.types;

import net.jadoth.memory.XMemory;

// CHECKSTYLE.OFF: AbstractClassName: this is kind of a hacky solution to improve readability on the use site
public abstract class Binary2
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	/**
	 * Depending on the deriving class, this is either a single entity's address for reading data
	 * or the beginning of a store chunk for storing multiple entities in a row (for efficiency reasons).
	 */
	long address;
	
	/**
	 * Needed in single-entity {@link BuildItem2} anyway and negligible in mass-entity implementations.
	 */
	private Object helper;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected Binary2()
	{
		super();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public abstract Chunk[] channelChunks();

	public abstract void storeEntityHeader(
		final long entityContentLength,
		final long entityTypeId       ,
		final long entityObjectId
	);
	

	// (15.01.2019 TM)TODO: check if #hasData can be replaced by or done in a smarter way.
	/**
	 * Some binary entries serve as a skip entry, so that an entry for a particular object id already exists.
	 * Naturally, those entries don't have data then, which must be checked (be checkable) later on.
	 *
	 * @return whether this instances carries (actually "knows") binary build data or not.
	 */
	public final boolean hasData()
	{
		return this.address != 0;
	}
	
	/**
	 * Helper instances can be used as temporary additional state for the duration of the building process.
	 * E.g.: JDK hash collections cannot properly collect elements during the building process as the element instances
	 * might still be in an initialized state without their proper data, so hashing and equality comparisons would
	 * fail or result in all elements being "equal". So building JDK hash collections required to pre-collect
	 * their elements in an additional helper structure and defer the actual elements collecting to the completion.
	 * <p>
	 * Similar problems with other or complex custom handlers are conceivable.
	 *<p>
	 * Only one helper object can be registered per subject instance (the instance to be built).
	 *
	 * @param subject
	 * @param helper
	 * @return
	 */
	public final synchronized void setHelper(final Object helper)
	{
		this.helper = helper;
	}

	/**
	 * Helper instances can be used as temporary additional state for the duration of the building process.
	 * E.g.: JDK hash collections cannot properly collect elements during the building process as the element instances
	 * might still be in an initialized state without their proper data, so hashing and equality comparisons would
	 * failt or result in all elements being "equal". So building JDK hash collections required to pre-collect
	 * their elements in an additional helper structure and defer the actual elements collecting to the completion.
	 * <p>
	 * Similar problems with other or complex custom handlers are conceivable.
	 *<p>
	 * Only one helper object can be registered per subject instance (the instance to be built).
	 *
	 * @param subject
	 * @return
	 */
	public final synchronized Object getHelper()
	{
		return this.helper;
	}

	public byte get_byte(final long offset)
	{
		return XMemory.get_byte(this.address + offset);
	}
	
	public boolean get_boolean(final long offset)
	{
		return XMemory.get_boolean(this.address + offset);
	}
	
	public short get_short(final long offset)
	{
		return XMemory.get_short(this.address + offset);
	}
	
	public char get_char(final long offset)
	{
		return XMemory.get_char(this.address + offset);
	}
	
	public int get_int(final long offset)
	{
		return XMemory.get_int(this.address + offset);
	}
	
	public float get_float(final long offset)
	{
		return XMemory.get_float(this.address + offset);
	}
	
	public long get_long(final long offset)
	{
		return XMemory.get_long(this.address + offset);
	}
	
	public double get_double(final long offset)
	{
		return XMemory.get_double(this.address + offset);
	}
	
	
	
	public void set_byte(final long offset, final byte value)
	{
		XMemory.set_byte(this.address + offset, value);
	}
	
	public void set_boolean(final long offset, final boolean value)
	{
		XMemory.set_boolean(this.address + offset, value);
	}
	
	public void set_short(final long offset, final short value)
	{
		XMemory.set_short(this.address + offset, value);
	}
	
	public void set_char(final long offset, final char value)
	{
		XMemory.set_char(this.address + offset, value);
	}
	
	public void set_int(final long offset, final int value)
	{
		XMemory.set_int(this.address + offset, value);
	}
	
	public void set_float(final long offset, final float value)
	{
		XMemory.set_float(this.address + offset, value);
	}
	
	public void set_long(final long offset, final long value)
	{
		XMemory.set_long(this.address + offset, value);
	}
	
	public void set_double(final long offset, final double value)
	{
		XMemory.set_double(this.address + offset, value);
	}
	
}
//CHECKSTYLE.ON: AbstractClassName
