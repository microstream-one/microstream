package net.jadoth.persistence.binary.types;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

// CHECKSTYLE.OFF: AbstractClassName: this is kind of a hacky solution to improve readability on the use site
public abstract class Binary implements Chunk
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	/*
	 * sneaky hardcoded field for performance reasons.
	 * Used only by build items for create/update address.
	 * A little hacky, but worth it.
	 * 
	 * (14.09.2018 TM)NOTE: is it really faster? Is it really worth it?
	 * Was it in the past? Is it still, with several years of JVM and JIT improvement?
	 * Would it be slower or maybe even faster to have the field be final?
	 * Or should the raw memory address not pollute the API?
	 */
	long entityContentAddress;

	private Object helperState;
	
	
	
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

	@Override
	public abstract ByteBuffer[] buffers();

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

	public abstract long[] startOffsets();

	public abstract long[] boundOffsets();

	public abstract long   entityContentAddress();

	/**
	 * Some binary entries serve as a skip entry, so that an entry for a particular object id already exists.
	 * Naturally, those entries don't have data then, which must be checked (be checkable) later on.
	 *
	 * @return whether this instances carries (actually "knows") binary build data or not.
	 */
	public final boolean hasData()
	{
		return this.entityContentAddress != 0;
	}

	// only for debug purposes!
	protected abstract void internalIterateCurrentData(Consumer<byte[]> iterator);

	protected abstract long[] internalGetStartOffsets();

	protected abstract long[] internalGetBoundOffsets();
	
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
		this.helperState = helper;
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
		return this.helperState;
	}
		
}
//CHECKSTYLE.ON: AbstractClassName

