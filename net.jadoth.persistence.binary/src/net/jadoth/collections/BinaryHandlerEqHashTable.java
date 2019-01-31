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
public final class BinaryHandlerEqHashTable
extends AbstractBinaryHandlerNativeCustomCollection<EqHashTable<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long
		BINARY_OFFSET_EQUALATOR    =                                                       0,
		BINARY_OFFSET_KEYS         = BINARY_OFFSET_EQUALATOR + Binary.oidByteLength(),
		BINARY_OFFSET_VALUES       = BINARY_OFFSET_KEYS      + Binary.oidByteLength(),
		BINARY_OFFSET_HASH_DENSITY = BINARY_OFFSET_VALUES    + Binary.oidByteLength(),
		BINARY_OFFSET_ELEMENTS     = BINARY_OFFSET_HASH_DENSITY + XMemory.byteSize_float()
	;

	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field
		FIELD_EQUALATOR = XReflect.getInstanceFieldOfType(EqHashTable.class, HashEqualator.class)     ,
		FIELD_KEYS      = XReflect.getInstanceFieldOfType(EqHashTable.class, EqHashTable.Keys.class)  ,
		FIELD_VALUES    = XReflect.getInstanceFieldOfType(EqHashTable.class, EqHashTable.Values.class)
	;



	///////////////////////////////////////////////////////////////////////////
	// static methods   //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<EqHashTable<?, ?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)EqHashTable.class;
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

	public BinaryHandlerEqHashTable()
	{
		// binary layout definition
		super(
			typeWorkaround(),
			AbstractBinaryHandlerNative.pseudoFields(
				pseudoField(HashEqualator.class, "hashEqualator"),
				pseudoField(EqHashTable.Keys.class, "keys"),
				pseudoField(EqHashTable.Values.class, "values"),
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
		final EqHashTable<?, ?>       instance,
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
	public final EqHashTable<?, ?> create(final Binary bytes)
	{
		return EqHashTable.NewCustom(
			getBuildItemElementCount(bytes),
			getBuildItemHashDensity(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final EqHashTable<?, ?> instance, final PersistenceLoadHandler builder)
	{
		@SuppressWarnings("unchecked") // necessary because this handler operates on a generic technical level
		final EqHashTable<Object, Object> collectingInstance = (EqHashTable<Object, Object>)instance;

		// set single instances (must be done on memory-level due to final modifier. Little hacky, but okay)
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_EQUALATOR),
			builder.lookupObject(bytes.get_long(BINARY_OFFSET_EQUALATOR))
		);
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
		instance.size = bytes.collectKeyValueReferences(
			BINARY_OFFSET_ELEMENTS,
			getBuildItemElementCount(bytes),
			builder,
			collectingInstance::internalCollectUnhashed
		);
		// note: hashDensity has already been set at creation time (shallow primitive value)
	}

	@Override
	public final void complete(final Binary medium, final EqHashTable<?, ?> instance, final PersistenceLoadHandler builder)
	{
		// rehash all previously unhashed collected elements
		instance.rehash();
	}

	@Override
	public final void iterateInstanceReferences(final EqHashTable<?, ?> instance, final PersistenceFunction iterator)
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
