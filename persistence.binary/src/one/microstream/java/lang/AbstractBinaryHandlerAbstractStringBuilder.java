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
	
	protected static final long LENGTH_LENGTH = Integer.BYTES;
	
	protected static final long
		OFFSET_LENGTH = 0                            ,
		OFFSET_CHARS  = OFFSET_LENGTH + LENGTH_LENGTH
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
		final int                     length  ,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		final long contentAddress = bytes.storeEntityHeader(
			(long)length * Character.BYTES + LENGTH_LENGTH, this.typeId(), objectId
		);
		
		bytes.store_int(contentAddress + OFFSET_LENGTH, data.length);
		bytes.storeCharsDirect(contentAddress + OFFSET_CHARS, data, 0, length);
	}
	
	protected final int readCapacity(final Binary bytes)
	{
		return X.checkArrayRange(bytes.get_long(OFFSET_LENGTH));
	}
	
	protected final int readLength(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getBuildItemContentLength() - LENGTH_LENGTH);
	}
	
	protected final void readChars(final Binary bytes, final char[] target)
	{
		final int lengthChars = X.checkArrayRange(bytes.getBuildItemContentLength() - LENGTH_LENGTH);
		bytes.readCharsDirect(bytes.getBuildItemContentLength() + OFFSET_CHARS, target, 0, lengthChars);
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
