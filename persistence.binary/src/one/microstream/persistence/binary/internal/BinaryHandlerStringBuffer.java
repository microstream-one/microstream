package one.microstream.persistence.binary.internal;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerStringBuffer extends AbstractBinaryHandlerAbstractStringBuilder<StringBuffer>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerStringBuffer()
	{
		super(StringBuffer.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final StringBuffer            instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		this.storeData(bytes, XMemory.accessChars(instance), instance.length(), objectId, handler);
	}

	@Override
	public final StringBuffer create(final Binary bytes)
	{
		return new StringBuffer(this.readCapacity(bytes));
	}

	@Override
	public void update(final Binary bytes, final StringBuffer instance, final PersistenceLoadHandler builder)
	{
		instance.ensureCapacity(this.readCapacity(bytes));
		this.readChars(bytes, XMemory.accessChars(instance));
	}

}
