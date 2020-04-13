package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.reflect.XReflect;


public final class BinaryHandlerStatelessConstant<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> BinaryHandlerStatelessConstant<T> New(final T constantInstance)
	{
		return new BinaryHandlerStatelessConstant<>(
			notNull(constantInstance)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final T constantInstance;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerStatelessConstant(final T constantInstance)
	{
		super(XReflect.getClass(constantInstance));
		this.constantInstance = constantInstance;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeStateless(this.typeId(), objectId);
	}

	@Override
	public final T create(final Binary data, final PersistenceLoadHandler handler)
	{
		return this.constantInstance;
	}

}
