package one.microstream.java.lang;

import one.microstream.chars.XChars;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerStringBuffer extends AbstractBinaryHandlerAbstractStringBuilder<StringBuffer>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

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
		this.storeData(bytes, XChars.toCharArray(instance), instance.capacity(), objectId, handler);
	}

	@Override
	public final StringBuffer create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new StringBuffer(this.readCapacity(bytes));
	}

	@Override
	public void update(final Binary bytes, final StringBuffer instance, final PersistenceLoadHandler handler)
	{
		// because implementing a clear() would have been too hard for the JDK Pros.
		instance.delete(0, instance.length());
		
		instance.ensureCapacity(this.readCapacity(bytes));
		instance.append(this.readChars(bytes));
	}

}
