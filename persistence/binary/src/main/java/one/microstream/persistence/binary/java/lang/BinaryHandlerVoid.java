package one.microstream.persistence.binary.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerStateless;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerVoid extends AbstractBinaryHandlerStateless<Void>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerVoid New()
	{
		return new BinaryHandlerVoid();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerVoid()
	{
		super(Void.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final Void                            instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final Void create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void updateState(
		final Binary                 data    ,
		final Void                   instance,
		final PersistenceLoadHandler handler
	)
	{
		throw new UnsupportedOperationException();
	}

}
