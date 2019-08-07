package one.microstream.java.lang;

import one.microstream.chars.XChars;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerStringBuilder extends AbstractBinaryHandlerAbstractStringBuilder<StringBuilder>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerStringBuilder New()
	{
		return new BinaryHandlerStringBuilder();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerStringBuilder()
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
		this.storeData(bytes, XChars.toCharArray(instance), instance.capacity(), objectId, handler);
	}

	@Override
	public final StringBuilder create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return new StringBuilder(this.readCapacity(bytes));
	}

	@Override
	public void update(final Binary bytes, final StringBuilder instance, final PersistenceObjectIdResolver idResolver)
	{
		// because implementing a clear() would have been too hard for the JDK Pros.
		instance.delete(0, instance.length());
		
		instance.ensureCapacity(this.readCapacity(bytes));
		instance.append(this.readChars(bytes));
	}

}
