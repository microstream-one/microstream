package one.microstream.java.lang;

import one.microstream.X;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceStoreHandler;

public abstract class AbstractBinaryHandlerAbstractStringBuilder<B/*extends AbstractStringBuilder*/>
extends AbstractBinaryHandlerCustom<B>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	protected static final long LENGTH_CAPACITY = Long.BYTES;
	
	protected static final long
		OFFSET_CAPACITY = 0                                ,
		OFFSET_CHARS    = OFFSET_CAPACITY + LENGTH_CAPACITY
	;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerAbstractStringBuilder(final Class<B> type)
	{
		super(
			type,
			pseudoFields(
				pseudoField(long.class, "capacity"),
				chars("value")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	protected final void storeData(
		final Binary                  bytes   ,
		final char[]                  data    ,
		final int                     capacity,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// capacity + list header + list data
		final long contentLength = Binary.toBinaryListTotalByteLength(
			LENGTH_CAPACITY + (long)data.length * Character.BYTES
		);
		
		final long contentAddress = bytes.storeEntityHeader(contentLength, this.typeId(), objectId);
		bytes.store_long(contentAddress + OFFSET_CAPACITY, capacity);
		bytes.storeCharsAsList(contentAddress + OFFSET_CHARS, data, 0, data.length);
	}
	
	protected final int readCapacity(final Binary bytes)
	{
		return X.checkArrayRange(bytes.get_long(OFFSET_CAPACITY));
	}
	
	protected final char[] readChars(final Binary bytes)
	{
		return bytes.buildArray_char(OFFSET_CHARS);
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return false;
	}
	
	@Override
	public final boolean hasPersistedVariableLength()
	{
		return true;
	}

	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

}
