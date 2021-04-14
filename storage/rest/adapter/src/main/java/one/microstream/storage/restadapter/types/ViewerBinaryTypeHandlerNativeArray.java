package one.microstream.storage.restadapter.types;

import java.lang.reflect.Array;

import one.microstream.persistence.binary.types.Binary;
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
	public ObjectDescription create(final Binary medium, final PersistenceLoadHandler handler)
	{
		final ObjectDescription objectDescription = new ObjectDescription();
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

		objectDescription.setValues(new Object[] {objArray});

		objectDescription.setLength(0);
		objectDescription.setVariableLength(new Long[] {(long) length});

		return objectDescription;
	}

}
