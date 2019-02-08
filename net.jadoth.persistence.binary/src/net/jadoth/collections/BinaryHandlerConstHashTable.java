package net.jadoth.collections;

import java.lang.reflect.Field;

import net.jadoth.X;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNative;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustomCollection;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceObjectIdAcceptor;
import net.jadoth.persistence.types.PersistenceStoreHandler;
import net.jadoth.reflect.XReflect;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerConstHashTable
extends AbstractBinaryHandlerNativeCustomCollection<ConstHashTable<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long
		BINARY_OFFSET_KEYS         = 0                                                  ,
		BINARY_OFFSET_VALUES       = BINARY_OFFSET_KEYS         + Binary.oidByteLength(),
		BINARY_OFFSET_HASH_DENSITY = BINARY_OFFSET_VALUES       + Binary.oidByteLength(),
		BINARY_OFFSET_ELEMENTS     = BINARY_OFFSET_HASH_DENSITY + Float.BYTES
	;

	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field
		FIELD_KEYS   = XReflect.getInstanceFieldOfType(ConstHashTable.class, ConstHashTable.Keys.class)  ,
		FIELD_VALUES = XReflect.getInstanceFieldOfType(ConstHashTable.class, ConstHashTable.Values.class)
	;



	///////////////////////////////////////////////////////////////////////////
	// static methods   //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<ConstHashTable<?, ?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)ConstHashTable.class;
	}

	private static int getBuildItemElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS));
	}

	private static float getBuildItemHashDensity(final Binary bytes)
	{
		return bytes.get_float(BINARY_OFFSET_HASH_DENSITY);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public BinaryHandlerConstHashTable()
	{
		// binary layout definition
		super(
			typeWorkaround(),
			AbstractBinaryHandlerNative.pseudoFields(
				pseudoField(ConstHashTable.Keys.class, "keys"),
				pseudoField(ConstHashTable.Values.class, "values"),
				pseudoField(float.class, "hashDensity"),
				AbstractBinaryHandlerNative.complex("entries",
					AbstractBinaryHandlerNative.pseudoField(Object.class, "key"),
					AbstractBinaryHandlerNative.pseudoField(Object.class, "value")
				)
			)

		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final ConstHashTable<?, ?>    instance,
		final long                    oid     ,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		final long contentAddress = bytes.storeSizedKeyValuesAsEntries(
			this.typeId()         ,
			oid                   ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);
		bytes.store_long(
			contentAddress + BINARY_OFFSET_KEYS,
			handler.apply(instance.keys)
		);
		bytes.store_long(
			contentAddress + BINARY_OFFSET_VALUES,
			handler.apply(instance.values)
		);
		bytes.store_float(
			contentAddress + BINARY_OFFSET_HASH_DENSITY,
			instance.hashDensity
		);
	}

	@Override
	public final ConstHashTable<?, ?> create(final Binary bytes)
	{
		return ConstHashTable.NewCustom(
			getBuildItemElementCount(bytes),
			getBuildItemHashDensity(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final ConstHashTable<?, ?> instance, final PersistenceLoadHandler builder)
	{
		@SuppressWarnings("unchecked") // necessary because this handler operates on a generic technical level
		final ConstHashTable<Object, Object> casted = (ConstHashTable<Object, Object>)instance;

		// validate to the best of possibilities
		if(instance.size != 0)
		{
			throw new IllegalStateException(); // (26.10.2013)EXCP: proper exception
		}

		// set satellite instances (must be done on memory-level due to final modifier. Little hacky, but okay)
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_KEYS),
			builder.lookupObject(bytes.get_long(BINARY_OFFSET_KEYS))
		);
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_VALUES),
			builder.lookupObject(bytes.get_long(BINARY_OFFSET_VALUES))
		);
		bytes.collectKeyValueReferences(
			BINARY_OFFSET_ELEMENTS,
			getBuildItemElementCount(bytes),
			builder,
			casted::internalAdd
		);
		// note: hashDensity has already been set at creation time (shallow primitive value)
	}

	@Override
	public final void iterateInstanceReferences(final ConstHashTable<?, ?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.keys);
		iterator.apply(instance.values);
		Persistence.iterateReferences(iterator, instance);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_KEYS));
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_VALUES));
		bytes.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}

}
