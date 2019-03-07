package net.jadoth.persistence.binary.internal;

import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceStoreHandler;


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
	public final StringBuilder create(final Binary bytes)
	{
		return new StringBuilder(this.readCapacity(bytes));
	}

	@Override
	public void update(final Binary bytes, final StringBuilder instance, final PersistenceLoadHandler builder)
	{
		instance.ensureCapacity(this.readCapacity(bytes));
		this.readChars(bytes, XMemory.accessChars(instance));
	}

}
