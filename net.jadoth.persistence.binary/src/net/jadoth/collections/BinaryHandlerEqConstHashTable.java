package net.jadoth.collections;

import java.lang.reflect.Field;

import net.jadoth.X;
import net.jadoth.functional._longProcedure;
import net.jadoth.hashing.HashEqualator;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNative;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustomCollection;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceStoreHandler;
import net.jadoth.reflect.XReflect;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerEqConstHashTable
extends AbstractBinaryHandlerNativeCustomCollection<EqConstHashTable<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long
		BINARY_OFFSET_EQUALATOR    =                                                       0,
		BINARY_OFFSET_KEYS         = BINARY_OFFSET_EQUALATOR + BinaryPersistence.oidByteLength(),
		BINARY_OFFSET_VALUES       = BINARY_OFFSET_KEYS      + BinaryPersistence.oidByteLength(),
		BINARY_OFFSET_HASH_DENSITY = BINARY_OFFSET_VALUES    + BinaryPersistence.oidByteLength(),
		BINARY_OFFSET_ELEMENTS     = BINARY_OFFSET_HASH_DENSITY + XMemory.byteSize_float()
	;

	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field
		FIELD_EQUALATOR = XReflect.getInstanceFieldOfType(EqConstHashTable.class, HashEqualator.class)          ,
		FIELD_KEYS      = XReflect.getInstanceFieldOfType(EqConstHashTable.class, EqConstHashTable.Keys.class)  ,
		FIELD_VALUES    = XReflect.getInstanceFieldOfType(EqConstHashTable.class, EqConstHashTable.Values.class)
	;



	///////////////////////////////////////////////////////////////////////////
	// static methods   //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<EqConstHashTable<?, ?>> typeWorkaround()
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



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerEqConstHashTable()
	{
		// binary layout definition
		super(
			typeWorkaround(),
			AbstractBinaryHandlerNative.pseudoFields(
				pseudoField(HashEqualator.class, "hashEqualator"),
				pseudoField(EqConstHashTable.Keys.class, "keys"),
				pseudoField(EqConstHashTable.Values.class, "values"),
				pseudoField(float.class, "hashDensity"),
				AbstractBinaryHandlerNative.complex("entries",
					AbstractBinaryHandlerNative.pseudoField(Object.class, "key"),
					AbstractBinaryHandlerNative.pseudoField(Object.class, "value")
				)
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
		XMemory.set_long(
			contentAddress + BINARY_OFFSET_EQUALATOR,
			handler.apply(instance.hashEqualator)
		);
		XMemory.set_long(
			contentAddress + BINARY_OFFSET_KEYS,
			handler.apply(instance.keys)
		);
		XMemory.set_long(
			contentAddress + BINARY_OFFSET_VALUES,
			handler.apply(instance.values)
		);
		XMemory.set_float(
			contentAddress + BINARY_OFFSET_HASH_DENSITY,
			instance.hashDensity
		);
	}

	@Override
	public final EqConstHashTable<?, ?> create(final Binary bytes)
	{
		return EqConstHashTable.NewCustom(
			getBuildItemElementCount(bytes),
			getBuildItemHashDensity(bytes)
		);
	}

	@Override
	public final void update(
		final Binary                 bytes   ,
		final EqConstHashTable<?, ?> instance,
		final PersistenceLoadHandler handler
	)
	{
		@SuppressWarnings("unchecked") // necessary because this handler operates on a generic technical level
		final EqConstHashTable<Object, Object> casted = (EqConstHashTable<Object, Object>)instance;

		// set single instances (must be done on memory-level due to final modifier. Little hacky, but okay)
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_EQUALATOR),
			handler.lookupObject(bytes.get_long(BINARY_OFFSET_EQUALATOR))
		);
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_KEYS),
			handler.lookupObject(bytes.get_long(BINARY_OFFSET_KEYS))
		);
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_VALUES),
			handler.lookupObject(bytes.get_long(BINARY_OFFSET_VALUES))
		);
		instance.size = bytes.collectKeyValueReferences(
			BINARY_OFFSET_ELEMENTS,
			getBuildItemElementCount(bytes),
			handler,
			casted::internalCollectUnhashed
		);
		// note: hashDensity has already been set at creation time (shallow primitive value)
	}

	@Override
	public final void complete(final Binary medium, final EqConstHashTable<?, ?> instance, final PersistenceLoadHandler builder)
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
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		iterator.accept(bytes.get_long(BINARY_OFFSET_EQUALATOR));
		iterator.accept(bytes.get_long(BINARY_OFFSET_KEYS));
		iterator.accept(bytes.get_long(BINARY_OFFSET_VALUES));
		bytes.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}

}
