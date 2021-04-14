package one.microstream.persistence.binary.java.lang;

import one.microstream.chars.XChars;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
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
		final Binary                          data    ,
		final StringBuilder                   instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		this.storeData(data, XChars.toCharArray(instance), instance.capacity(), objectId, handler);
	}

	@Override
	public final StringBuilder create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new StringBuilder(this.readCapacity(data));
	}

	@Override
	public void updateState(final Binary data, final StringBuilder instance, final PersistenceLoadHandler handler)
	{
		// because implementing a clear() would have been too hard for the JDK Pros.
		instance.delete(0, instance.length());
		
		instance.ensureCapacity(this.readCapacity(data));
		instance.append(this.readChars(data));
	}

}
