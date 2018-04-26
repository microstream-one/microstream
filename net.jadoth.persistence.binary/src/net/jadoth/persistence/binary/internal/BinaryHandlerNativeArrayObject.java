package net.jadoth.persistence.binary.internal;

import static net.jadoth.util.JadothTypes.validateArrayType;
import static net.jadoth.util.JadothTypes.validateNonPrimitiveType;

import java.lang.reflect.Array;

import net.jadoth.Jadoth;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.objectstate.ObjectState;
import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;
import net.jadoth.swizzling.types.PersistenceStoreFunction;

public final class BinaryHandlerNativeArrayObject<A/*extends Object[]*/> extends AbstractBinaryHandlerNativeArray<A>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////
	
	static final long BINARY_OFFSET_ELEMENTS = 0L;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final           Class<A> arrayType    ;
	private final transient Class<?> componentType;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArrayObject(final Class<A> arrayType, final long typeId)
	{
		super(typeId, validateArrayType(arrayType), defineElementsType(arrayType.getComponentType()));
		this.arrayType     = arrayType;
		this.componentType = validateNonPrimitiveType(arrayType.getComponentType());
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public final Class<A> getArrayType()
	{
		return this.arrayType;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(final Binary bytes, final A instance, final long oid, final PersistenceStoreFunction linker)
	{
		BinaryPersistence.storeArrayContentAsList(
			bytes                      ,
			this.typeId()              ,
			oid                        ,
			0                          ,
			linker                  ,
			(Object[])instance         ,
			0                          ,
			((Object[])instance).length
		);
	}

	@Override
	public final A create(final Binary bytes)
	{
		final long rawElementCount = BinaryPersistence.getListElementCount(bytes, BINARY_OFFSET_ELEMENTS);
		return this.arrayType.cast(
			Array.newInstance(this.componentType, Jadoth.checkArrayRange(rawElementCount))
		);
	}

	@Override
	public final void update(final Binary bytes, final A instance, final SwizzleBuildLinker builder)
	{
		// better check length consistency here
		final Object[] arrayInstance = (Object[])instance;
		BinaryCollectionHandling.validateArrayLength(arrayInstance, bytes, BINARY_OFFSET_ELEMENTS);
	
		BinaryPersistence.collectElementsIntoArray(bytes, BINARY_OFFSET_ELEMENTS, builder, arrayInstance);
	}
	
	@Override
	public final void iterateInstanceReferences(final A instance, final SwizzleFunction iterator)
	{
		Swizzle.iterateReferences(iterator, (Object[])instance, 0, ((Object[])instance).length);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		BinaryPersistence.iterateListElementReferences(bytes, BINARY_OFFSET_ELEMENTS, iterator);
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

	@Override
	public boolean isEqual(final A source, final A target, final ObjectStateHandlerLookup stateHandlerLookup)
	{
		return Array.getLength(source) == Array.getLength(target)
			&& ObjectState.isEqual((Object[])source, (Object[])target, 0, Array.getLength(source), stateHandlerLookup)
		;
	}

}
