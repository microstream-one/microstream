package one.microstream.java.lang;

import java.lang.reflect.Array;

import one.microstream.X;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.reflect.XReflect;

public final class BinaryHandlerNativeArrayObject<A/*extends Object[]*/> extends AbstractBinaryHandlerNativeArray<A>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	static final long BINARY_OFFSET_ELEMENTS = 0L;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final           Class<A> arrayType    ;
	private final transient Class<?> componentType;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerNativeArrayObject(final Class<A> arrayType)
	{
		super(
			XReflect.validateArrayType(arrayType),
			defineElementsType(arrayType.getComponentType())
		);
		this.arrayType     = arrayType;
		this.componentType = XReflect.validateNonPrimitiveType(arrayType.getComponentType());
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public final Class<A> getArrayType()
	{
		return this.arrayType;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final A                       instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		bytes.storeReferences(
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
	public final A create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		final long rawElementCount = bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS);
		return this.arrayType.cast(
			Array.newInstance(this.componentType, X.checkArrayRange(rawElementCount))
		);
	}

	@Override
	public final void update(final Binary bytes, final A instance, final PersistenceLoadHandler handler)
	{
		final Object[] arrayInstance = (Object[])instance;
		
		// better check length consistency here. No clear required.
		bytes.validateArrayLength(arrayInstance, BINARY_OFFSET_ELEMENTS);
		bytes.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, handler, arrayInstance);
	}
	
	@Override
	public final void iterateInstanceReferences(final A instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, (Object[])instance, 0, ((Object[])instance).length);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return true;
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}

}
