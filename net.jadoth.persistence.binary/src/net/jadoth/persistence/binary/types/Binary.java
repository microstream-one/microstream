package net.jadoth.persistence.binary.types;

import java.nio.ByteBuffer;

import net.jadoth.memory.XMemory;

// CHECKSTYLE.OFF: AbstractClassName: this is kind of a hacky solution to improve readability on the use site
public abstract class Binary implements Chunk
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
	
	protected Binary()
	{
		super();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	// (25.01.2019 TM)NOTE: new with JET-49
	
	public abstract Binary channelChunk(int channelIndex);
	
	public abstract int channelCount();
	
	public abstract void iterateEntityData(BinaryEntityDataReader reader);
	
		
	// (25.01.2019 TM)NOTE: kept with JET-49
	
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
		if(this.helper instanceof HelperAnchor)
		{
			HelperAnchor anchor = (HelperAnchor)this.helper;
			while(anchor.actualHelper instanceof HelperAnchor)
			{
				anchor = (HelperAnchor)anchor.actualHelper;
			}
			anchor.actualHelper = helper;
		}
		else
		{
			this.helper = helper;
		}
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
		if(this.helper instanceof HelperAnchor)
		{
			HelperAnchor anchor = (HelperAnchor)this.helper;
			while(anchor.actualHelper instanceof HelperAnchor)
			{
				anchor = (HelperAnchor)anchor.actualHelper;
			}
			return anchor.actualHelper;
		}
		
		return this.helper;
	}
	
	public final synchronized void anchorHelper(final Object anchorSubject)
	{
		this.helper = new HelperAnchor(anchorSubject, this.helper);
	}
	
	/**
	 * In rare cases (legacy type mapping), a direct byte buffer must be "anchored" in order to not get gc-collected
	 * and cause its memory to be deallocated. Anchoring means it just has to be referenced by anything that lives
	 * until the end of the entity loading/building process. It never has to be dereferenced again.
	 * In order to not need another fixed field, which would needlessly occupy memory for EVERY entity in almost every
	 * case, a "helper anchor" is used: a nifty instance that is clamped in between the actual load item and the actual
	 * helper instance.
	 * 
	 * @author TM
	 *
	 */
	static final class HelperAnchor
	{
		final Object anchorSubject;
		      Object actualHelper;
		
		HelperAnchor(final Object anchorSubject, final Object actualHelper)
		{
			super();
			this.anchorSubject = anchorSubject;
			this.actualHelper  = actualHelper;
		}
		
	}
	
	
	
	// (25.01.2019 TM)FIXME: temporary for JET-49
	
	/**
	 * Writes the header (etc...).
	 * <p>
	 * Returns a memory address that is guaranteed to be safe for writing {@literal len} bytes.
	 * Writing any more bytes will lead to unpredictable results, from (most likely) destroying
	 * the byte stream's consistency up to crashing the VM immediately or at some point in the future.
	 * <p>
	 * DO NOT WRITE MORE THEN {@literal len} BYTES TO THE RETURNED ADDRESS!
	 *
	 * @param entityContentLength
	 * @param entityTypeId
	 * @param entityObjectId
	 * @return
	 */
	public abstract long storeEntityHeader(
		final long entityContentLength,
		final long entityTypeId       ,
		final long entityObjectId
	);
	
	public abstract long loadItemEntityContentAddress();
	
	public abstract void setLoadItemEntityContentAddress(long entityContentAddress);
	

	/* (25.01.2019 TM)FIXME: JET-49: excplizit sets to a memory address should not be possible
	 * better: a working address and offset-less setters with internal address advancing.
	 */

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
	
	
	
	// (25.01.2019 TM)FIXME: old before JET-49

	@Override
	public abstract ByteBuffer[] buffers();
	
}
//CHECKSTYLE.ON: AbstractClassName

