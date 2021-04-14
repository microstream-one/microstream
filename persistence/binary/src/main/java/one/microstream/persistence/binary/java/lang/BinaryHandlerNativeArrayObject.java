package one.microstream.persistence.binary.java.lang;

import static one.microstream.X.notNull;

import java.lang.reflect.Array;

import one.microstream.X;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.reflect.XReflect;

public final class BinaryHandlerNativeArrayObject<A/*extends Object[]*/> extends AbstractBinaryHandlerNativeArray<A>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	static final long BINARY_OFFSET_ELEMENTS = 0L;

	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> BinaryHandlerNativeArrayObject<T> New(final Class<T> type)
	{
		return new BinaryHandlerNativeArrayObject<>(
			notNull(type)
		);
	}
			
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final           Class<A> arrayType    ;
	private final transient Class<?> componentType;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerNativeArrayObject(final Class<A> arrayType)
	{
		super(
			XReflect.validateArrayType(arrayType),
			defineElementsType(arrayType.getComponentType())
		);
		this.arrayType     = arrayType;
		this.componentType = XReflect.validateNonPrimitiveType(arrayType.getComponentType());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public final Class<A> getArrayType()
	{
		return this.arrayType;
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}

	@Override
	public final void store(
		final Binary                          data    ,
		final A                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeReferences(
			this.typeId()              ,
			objectId                   ,
			0                          ,
			handler                    ,
			(Object[])instance         ,
			0                          ,
			((Object[])instance).length
		);
	}

	@Override
	public final A create(final Binary data, final PersistenceLoadHandler handler)
	{
		final long rawElementCount = data.getListElementCountReferences(BINARY_OFFSET_ELEMENTS);
		return this.arrayType.cast(
			Array.newInstance(this.componentType, X.checkArrayRange(rawElementCount))
		);
	}

	@Override
	public final void updateState(final Binary data, final A instance, final PersistenceLoadHandler handler)
	{
		final Object[] arrayInstance = (Object[])instance;
		
		// better check length consistency here. No clear required.
		data.validateArrayLength(arrayInstance, BINARY_OFFSET_ELEMENTS);
		data.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, handler, arrayInstance);
	}
	
	@Override
	public final void iterateInstanceReferences(final A instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, (Object[])instance, 0, ((Object[])instance).length);
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		data.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}

}
