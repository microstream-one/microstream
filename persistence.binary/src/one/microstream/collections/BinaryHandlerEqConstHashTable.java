package one.microstream.collections;

import java.lang.reflect.Field;

import one.microstream.X;
import one.microstream.hashing.HashEqualator;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerEqConstHashTable
extends AbstractBinaryHandlerCustomCollection<EqConstHashTable<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long
		BINARY_OFFSET_EQUALATOR    =                                                        0,
		BINARY_OFFSET_KEYS         = BINARY_OFFSET_EQUALATOR    + Binary.objectIdByteLength(),
		BINARY_OFFSET_VALUES       = BINARY_OFFSET_KEYS         + Binary.objectIdByteLength(),
		BINARY_OFFSET_HASH_DENSITY = BINARY_OFFSET_VALUES       + Binary.objectIdByteLength(),
		BINARY_OFFSET_ELEMENTS     = BINARY_OFFSET_HASH_DENSITY + Float.BYTES
	;

	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field
		FIELD_EQUALATOR = getInstanceFieldOfType(EqConstHashTable.class, HashEqualator.class)          ,
		FIELD_KEYS      = getInstanceFieldOfType(EqConstHashTable.class, EqConstHashTable.Keys.class)  ,
		FIELD_VALUES    = getInstanceFieldOfType(EqConstHashTable.class, EqConstHashTable.Values.class)
	;



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<EqConstHashTable<?, ?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)EqConstHashTable.class;
	}

	private static int getBuildItemElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS));
	}

	private static float getBuildItemHashDensity(final Binary bytes)
	{
		return bytes.get_float(BINARY_OFFSET_HASH_DENSITY);
	}
	
	public static BinaryHandlerEqConstHashTable New()
	{
		return new BinaryHandlerEqConstHashTable();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerEqConstHashTable()
	{
		// binary layout definition
		super(
			handledType(),
			keyValuesFields(
				CustomField(HashEqualator.class, "hashEqualator"),
				CustomField(EqConstHashTable.Keys.class, "keys"),
				CustomField(EqConstHashTable.Values.class, "values"),
				CustomField(float.class, "hashDensity")
			)

		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final EqConstHashTable<?, ?>  instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		bytes.storeKeyValuesAsEntries(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);
		bytes.store_long(
			BINARY_OFFSET_EQUALATOR,
			handler.apply(instance.hashEqualator)
		);
		bytes.store_long(
			BINARY_OFFSET_KEYS,
			handler.apply(instance.keys)
		);
		bytes.store_long(
			BINARY_OFFSET_VALUES,
			handler.apply(instance.values)
		);
		bytes.store_float(
			BINARY_OFFSET_HASH_DENSITY,
			instance.hashDensity
		);
	}

	@Override
	public final EqConstHashTable<?, ?> create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return EqConstHashTable.NewCustom(
			getBuildItemElementCount(bytes),
			getBuildItemHashDensity(bytes)
		);
	}

	@Override
	public final void update(
		final Binary                      bytes     ,
		final EqConstHashTable<?, ?>      instance  ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		// validate to the best of possibilities (or should an immutable instance be updatedable from outside?)
		if(instance.size != 0)
		{
			throw new IllegalStateException(); // (28.10.2013)EXCP: proper exception
		}
		
		@SuppressWarnings("unchecked") // necessary because this handler operates on a generic technical level
		final EqConstHashTable<Object, Object> casted = (EqConstHashTable<Object, Object>)instance;

		// set single instances (must be done on memory-level due to final modifier. Little hacky, but okay)
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_EQUALATOR),
			idResolver.lookupObject(bytes.get_long(BINARY_OFFSET_EQUALATOR))
		);
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_KEYS),
			idResolver.lookupObject(bytes.get_long(BINARY_OFFSET_KEYS))
		);
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_VALUES),
			idResolver.lookupObject(bytes.get_long(BINARY_OFFSET_VALUES))
		);
		instance.size = bytes.collectKeyValueReferences(
			BINARY_OFFSET_ELEMENTS,
			getBuildItemElementCount(bytes),
			idResolver,
			casted::internalCollectUnhashed
		);
		// note: hashDensity has already been set at creation time (shallow primitive value)
	}

	@Override
	public final void complete(final Binary medium, final EqConstHashTable<?, ?> instance, final PersistenceObjectIdResolver idResolver)
	{
		// rehash all previously unhashed collected elements
		instance.internalRehash();
	}

	@Override
	public final void iterateInstanceReferences(final EqConstHashTable<?, ?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.hashEqualator);
		iterator.apply(instance.keys);
		iterator.apply(instance.values);
		Persistence.iterateReferences(iterator, instance);
	}

	@Override
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_EQUALATOR));
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_KEYS));
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_VALUES));
		bytes.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}

}
