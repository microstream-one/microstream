package one.microstream.persistence.binary.types;

import java.lang.reflect.Array;

import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceTypeHandler;

public class ViewerBinaryTypeHandlerNativeArray<T> extends ViewerBinaryTypeHandlerWrapperAbstract<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerBinaryTypeHandlerNativeArray(final PersistenceTypeHandler<Binary, T> nativeHandler)
	{
		super(nativeHandler);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public ViewerMemberProvider create(final Binary medium, final PersistenceObjectIdResolver idResolver)
	{
		final ViewerObjectDescription objectDescription = new ViewerObjectDescription();
		objectDescription.setObjectId(medium.getBuildItemObjectId());
		objectDescription.setPersistenceTypeDefinition(this.nativeHandler);

		final T value = this.nativeHandler.create(medium, idResolver);
		this.nativeHandler.update(medium, value, idResolver);

		final int l = Array.getLength(value);
		final Object objArray[] = new Object[l];
		for(int i = 0; i < l; i++)
		{
			objArray[i] = Array.get(value, i);
		}
		objectDescription.setValues(new Object[] {objArray});

		return objectDescription;
	}

}
