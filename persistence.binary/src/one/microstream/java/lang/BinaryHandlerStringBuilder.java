package one.microstream.java.lang;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerStringBuilder extends AbstractBinaryHandlerAbstractStringBuilder<StringBuilder>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerStringBuilder()
	{
		super(StringBuilder.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final StringBuilder           instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		this.storeData(bytes, XMemory.accessChars(instance), instance.length(), objectId, handler);
	}

	@Override
	public final StringBuilder create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new StringBuilder(this.readCapacity(bytes));
	}

	@Override
	public void update(final Binary bytes, final StringBuilder instance, final PersistenceLoadHandler handler)
	{
		// because implementing a clear() would have been too hard for the JDK Pros.
		instance.delete(0, instance.length());
		
		instance.ensureCapacity(this.readCapacity(bytes));
		this.readChars(bytes, XMemory.accessChars(instance));
	}

}
