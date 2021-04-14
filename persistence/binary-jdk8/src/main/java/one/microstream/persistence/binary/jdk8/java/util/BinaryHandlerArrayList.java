package one.microstream.persistence.binary.jdk8.java.util;

import static one.microstream.X.notNull;

import java.util.ArrayList;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomIterableSizedArray;
import one.microstream.persistence.binary.jdk8.types.SunJdk8Internals;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerArrayList
extends AbstractBinaryHandlerCustomIterableSizedArray<ArrayList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_SIZED_ARRAY = 0; // binary form is 100% just a sized array, so offset 0



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ArrayList<?>> handledType()
	{
		return (Class)ArrayList.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerArrayList New(final PersistenceSizedArrayLengthController controller)
	{
		return new BinaryHandlerArrayList(
			notNull(controller)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerArrayList(final PersistenceSizedArrayLengthController controller)
	{
		super(
			handledType(),
			SizedArrayFields(),
			controller
		);
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final void store(
		final Binary                          bytes   ,
		final ArrayList<?>                    instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		bytes.storeSizedArray(
			this.typeId()                  ,
			objectId                       ,
			BINARY_OFFSET_SIZED_ARRAY      ,
			SunJdk8Internals.accessArray(instance),
			instance.size()                ,
			handler
		);
	}

	@Override
	public final ArrayList<?> create(final Binary bytes, final PersistenceLoadHandler idResolver)
	{
		final int arrayLength = this.determineArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY);
		
		/*
		 * InitialCapacity 1 instead of default constructor is a workaround for yet another JDK bug.
		 * Using the default constructor causes #ensureCapacity to yield incorrect behavior for values of
		 * 10 or below, which causes a subsequent array length validation exception.
		 * Also see https://bugs.openjdk.java.net/browse/JDK-8206945
		 * 
		 * However, having an actually zero-capacity instance should still cause the internal dummy array instance
		 * to be used instead of a redundant one that unnecessarily occupies memory. Hence the if.
		 */
		return arrayLength == 0
			? new ArrayList<>(0)
			: new ArrayList<>(1)
		;
	}

	@Override
	public final void updateState(final Binary bytes, final ArrayList<?> instance, final PersistenceLoadHandler handler)
	{
		// instance must be cleared in case an existing one is updated
		instance.clear();
		
		// length must be checked for consistency reasons
		final int arrayLength = this.determineArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY);
		
		// check for the zero-capacity case. See #create.
		if(arrayLength == 0)
		{
			// no-op if the empty dummy array is already used, otherwise memory optimization (e.g. existing instance).
			instance.trimToSize();
			return;
		}
		
		// normal (non-zero-capacity) case: ensure capacity, add elements, adjust the size.
		instance.ensureCapacity(arrayLength);
		final int size = bytes.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY,
			handler,
			SunJdk8Internals.accessArray(instance)
		);
		SunJdk8Internals.setSize(instance, size);
	}

	@Override
	public final void iterateInstanceReferences(final ArrayList<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, SunJdk8Internals.accessArray(instance), 0, instance.size());
	}

	@Override
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceReferenceLoader iterator)
	{
		bytes.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}

}
