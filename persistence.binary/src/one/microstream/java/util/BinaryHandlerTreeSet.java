package one.microstream.java.util;

import java.util.NavigableMap;
import java.util.TreeSet;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerTreeSet extends AbstractBinaryHandlerCustom<TreeSet<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_BACKING_MAP = 0;



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<TreeSet<?>> typeWorkaround()
	{
		return (Class)TreeSet.class; // no idea how to get ".class" to work otherwise
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerTreeSet()
	{
		super(
			typeWorkaround(),
			pseudoFields(
				pseudoField(NavigableMap.class, "backingMap")
			)
		);
		
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final TreeSet<?>              instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		final long contentAddress = bytes.storeEntityHeader(
			Binary.objectIdByteLength(),
			this.typeId(),
			objectId
		);
		
		bytes.store_long(
			contentAddress,
			handler.apply(XMemory.accessBackingMap(instance))
		);
	}
	
	@Override
	public final TreeSet<?> create(final Binary bytes)
	{
		return new TreeSet<>();
	}

	@Override
	public final void update(final Binary bytes, final TreeSet<?> instance, final PersistenceLoadHandler builder)
	{
		XMemory.setBackingMap(
			instance,
			(NavigableMap<?, ?>) builder.lookupObject(bytes.get_long(BINARY_OFFSET_BACKING_MAP))
		);
	}

	@Override
	public final void iterateInstanceReferences(final TreeSet<?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(XMemory.accessBackingMap(instance));
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_BACKING_MAP));
	}
	
	// (20.03.2019 TM)FIXME: MS-76: why are there here?
		
	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

	@Override
	public boolean hasPersistedReferences()
	{
		return true;
	}

	@Override
	public boolean hasPersistedVariableLength()
	{
		return false;
	}

	@Override
	public boolean hasInstanceReferences()
	{
		return true;
	}
}
