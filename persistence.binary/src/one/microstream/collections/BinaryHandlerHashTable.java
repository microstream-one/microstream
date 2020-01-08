package one.microstream.collections;

import java.lang.reflect.Field;

import one.microstream.X;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerHashTable
extends AbstractBinaryHandlerCustomCollection<HashTable<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_KEYS         =                                                        0;
	static final long BINARY_OFFSET_VALUES       = BINARY_OFFSET_KEYS         + Binary.objectIdByteLength();
	static final long BINARY_OFFSET_HASH_DENSITY = BINARY_OFFSET_VALUES       + Binary.objectIdByteLength();
	static final long BINARY_OFFSET_ELEMENTS     = BINARY_OFFSET_HASH_DENSITY +                 Float.BYTES;

	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field FIELD_KEYS   = getInstanceFieldOfType(HashTable.class, HashTable.Keys.class);
	static final Field FIELD_VALUES = getInstanceFieldOfType(HashTable.class, HashTable.Values.class);



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<HashTable<?, ?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)HashTable.class;
	}

	private static int getBuildItemElementCount(final Binary data)
	{
		return X.checkArrayRange(data.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS));
	}

	private static float getBuildItemHashDensity(final Binary data)
	{
		return data.read_float(BINARY_OFFSET_HASH_DENSITY);
	}
	
	public static BinaryHandlerHashTable New()
	{
		return new BinaryHandlerHashTable();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerHashTable()
	{
		// binary layout definition
		super(
			handledType(),
			keyValuesFields(
				CustomField(HashTable.Keys.class, "keys"),
				CustomField(HashTable.Values.class, "values"),
				CustomField(float.class, "hashDensity")
			)

		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  data    ,
		final HashTable<?, ?>         instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		data.storeKeyValuesAsEntries(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);
		data.store_long(
			BINARY_OFFSET_KEYS,
			handler.apply(instance.keys)
		);
		data.store_long(
			BINARY_OFFSET_VALUES,
			handler.apply(instance.values)
		);
		data.store_float(
			BINARY_OFFSET_HASH_DENSITY,
			instance.hashDensity
		);
	}

	@Override
	public final HashTable<?, ?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return HashTable.NewCustom(
			getBuildItemElementCount(data),
			getBuildItemHashDensity(data)
		);
	}

	@Override
	public final void updateState(
		final Binary                 data    ,
		final HashTable<?, ?>        instance,
		final PersistenceLoadHandler handler
	)
	{
		// must clear to ensure consistency
		instance.clear();
		
		@SuppressWarnings("unchecked") // necessary because this handler operates on a generic technical level
		final HashTable<Object, Object> collectingInstance = (HashTable<Object, Object>)instance;

		// set satellite instances (must be done on memory-level due to final modifier. Little hacky, but okay)
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_KEYS),
			handler.lookupObject(data.read_long(BINARY_OFFSET_KEYS))
		);
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_VALUES),
			handler.lookupObject(data.read_long(BINARY_OFFSET_VALUES))
		);
		data.collectKeyValueReferences(
			BINARY_OFFSET_ELEMENTS,
			getBuildItemElementCount(data),
			handler,
			collectingInstance::internalAdd
		);
		// note: hashDensity has already been set at creation time (shallow primitive value)
	}

	@Override
	public final void iterateInstanceReferences(final HashTable<?, ?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.keys);
		iterator.apply(instance.values);
		Persistence.iterateReferences(iterator, instance);
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		iterator.acceptObjectId(data.read_long(BINARY_OFFSET_KEYS));
		iterator.acceptObjectId(data.read_long(BINARY_OFFSET_VALUES));
		data.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}

}
