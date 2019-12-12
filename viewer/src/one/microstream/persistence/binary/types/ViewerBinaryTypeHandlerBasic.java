package one.microstream.persistence.binary.types;

import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceTypeHandler;

public class ViewerBinaryTypeHandlerBasic<T> extends ViewerBinaryTypeHandlerWrapperAbstract<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerBinaryTypeHandlerBasic(final PersistenceTypeHandler<Binary, T> nativeHandler,
			final ViewerBinaryTypeHandlerGeneric genericHandler)
	{
		super(nativeHandler, genericHandler);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public ViewerObjectDescription create(final Binary medium, final PersistenceObjectIdResolver idResolver)
	{
		final ViewerObjectDescription objectDescription = this.genericHandler.create(medium, idResolver);
		objectDescription.setPrimitiveInstance(this.nativeHandler.create(medium, idResolver));

		return objectDescription;
	}

	@SuppressWarnings("unchecked") // safe by logic
	@Override
	public void update(final Binary medium, final Object instance, final PersistenceObjectIdResolver idResolver)
	{
		this.nativeHandler.update(medium, (T)((ViewerObjectDescription)instance).getPrimitiveInstance(), idResolver);
	}

}
