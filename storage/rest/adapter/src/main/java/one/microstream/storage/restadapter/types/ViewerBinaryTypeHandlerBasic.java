package one.microstream.storage.restadapter.types;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
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
	public ObjectDescription create(final Binary medium, final PersistenceLoadHandler handler)
	{
		final ObjectDescription objectDescription = this.genericHandler.create(medium, handler);
		objectDescription.setPrimitiveInstance(this.nativeHandler.create(medium, handler));

		return objectDescription;
	}

	@SuppressWarnings("unchecked") // safe by logic
	@Override
	public void updateState(final Binary medium, final Object instance, final PersistenceLoadHandler handler)
	{
		this.nativeHandler.updateState(medium, (T)((ObjectDescription)instance).getPrimitiveInstance(), handler);
	}

}
