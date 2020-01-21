package one.microstream.persistence.binary.types;

import java.lang.reflect.Array;

import one.microstream.persistence.types.PersistenceLoadHandler;
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
	public ViewerObjectDescription create(final Binary medium, final PersistenceLoadHandler handler)
	{
		final ViewerObjectDescription objectDescription = new ViewerObjectDescription();
		objectDescription.setObjectId(medium.getBuildItemObjectId());
		objectDescription.setPersistenceTypeDefinition(this.nativeHandler);

		final T value = this.nativeHandler.create(medium, handler);
		this.nativeHandler.updateState(medium, value, handler);

		final int length = Array.getLength(value);
		final Object objArray[] = new Object[length];
		for(int i = 0; i < length; i++)
		{
			objArray[i] = Array.get(value, i);
		}

		objectDescription.setValues(objArray);
		objectDescription.setLength(length);

		return objectDescription;
	}

}
