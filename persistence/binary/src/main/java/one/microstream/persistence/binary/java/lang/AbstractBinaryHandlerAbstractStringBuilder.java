package one.microstream.persistence.binary.java.lang;

import one.microstream.X;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceReferenceLoader;
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
			CustomFields(
				CustomField(long.class, "capacity"),
				chars("value")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	protected final void storeData(
		final Binary                          data    ,
		final char[]                          chars   ,
		final int                             capacity,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// capacity + list header + list data
		final long contentLength = Binary.toBinaryListTotalByteLength(
			LENGTH_CAPACITY + (long)chars.length * Character.BYTES
		);
		
		data.storeEntityHeader(contentLength, this.typeId(), objectId);
		data.store_long(OFFSET_CAPACITY, capacity);
		data.storeCharsAsList(OFFSET_CHARS, chars, 0, chars.length);
	}
	
	protected final int readCapacity(final Binary data)
	{
		return X.checkArrayRange(data.read_long(OFFSET_CAPACITY));
	}
	
	protected final char[] readChars(final Binary data)
	{
		return data.build_chars(OFFSET_CHARS);
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
	
	@Override
	public final void iterateLoadableReferences(
		final Binary                     data    ,
		final PersistenceReferenceLoader iterator
	)
	{
		// references to be loaded
	}

}
