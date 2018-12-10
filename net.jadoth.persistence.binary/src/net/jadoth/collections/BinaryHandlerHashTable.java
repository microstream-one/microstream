package net.jadoth.collections;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;

import net.jadoth.X;
import net.jadoth.functional._longProcedure;
import net.jadoth.low.XVM;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNative;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustomCollection;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
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
public final class BinaryHandlerHashTable
extends AbstractBinaryHandlerNativeCustomCollection<HashTable<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_KEYS         =                                                    0;
	static final long BINARY_OFFSET_VALUES       = BINARY_OFFSET_KEYS   + BinaryPersistence.oidLength();
	static final long BINARY_OFFSET_HASH_DENSITY = BINARY_OFFSET_VALUES + BinaryPersistence.oidLength();
	static final long BINARY_OFFSET_ELEMENTS     = BINARY_OFFSET_HASH_DENSITY + XVM.byteSize_float();

	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field FIELD_KEYS   = XReflect.getInstanceFieldOfType(HashTable.class, HashTable.Keys.class);
	static final Field FIELD_VALUES = XReflect.getInstanceFieldOfType(HashTable.class, HashTable.Values.class);



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<HashTable<?, ?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)HashTable.class;
	}

	private static int getBuildItemElementCount(final Binary bytes)
	{
		return X.checkArrayRange(BinaryPersistence.getListElementCount(bytes, BINARY_OFFSET_ELEMENTS));
	}

	private static float getBuildItemHashDensity(final Binary bytes)
	{
		return BinaryPersistence.get_float(bytes, BINARY_OFFSET_HASH_DENSITY);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerHashTable()
	{
		// binary layout definition
		super(
			typeWorkaround(),
			AbstractBinaryHandlerNative.pseudoFields(
				pseudoField(HashTable.Keys.class, "keys"),
				pseudoField(HashTable.Values.class, "values"),
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
		final HashTable<?, ?>         instance,
		final long                    oid     ,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		final long contentAddress = BinaryCollectionHandling.storeSizedKeyValuesAsEntries(
			bytes                 ,
			this.typeId()         ,
			oid                   ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);
		XVM.set_long(
			contentAddress + BINARY_OFFSET_KEYS,
			handler.apply(instance.keys)
		);
		XVM.set_long(
			contentAddress + BINARY_OFFSET_VALUES,
			handler.apply(instance.values)
		);
		XVM.set_float(
			contentAddress + BINARY_OFFSET_HASH_DENSITY,
			instance.hashDensity
		);
	}

	@Override
	public final HashTable<?, ?> create(final Binary bytes)
	{
		return HashTable.NewCustom(
			getBuildItemElementCount(bytes),
			getBuildItemHashDensity(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final HashTable<?, ?> instance, final PersistenceLoadHandler builder)
	{
		@SuppressWarnings("unchecked") // necessary because this handler operates on a generic technical level
		final HashTable<Object, Object> collectingInstance = (HashTable<Object, Object>)instance;

		// set satellite instances (must be done on memory-level due to final modifier. Little hacky, but okay)
		XVM.setObject(
			instance,
			XVM.objectFieldOffset(FIELD_KEYS),
			builder.lookupObject(BinaryPersistence.get_long(bytes, BINARY_OFFSET_KEYS))
		);
		XVM.setObject(
			instance,
			XVM.objectFieldOffset(FIELD_VALUES),
			builder.lookupObject(BinaryPersistence.get_long(bytes, BINARY_OFFSET_VALUES))
		);
		BinaryPersistence.collectKeyValueReferences(
			bytes,
			BINARY_OFFSET_ELEMENTS,
			getBuildItemElementCount(bytes),
			builder,
			new BiConsumer<Object, Object>()
			{
				@Override
				public void accept(final Object key, final Object value)
				{
					collectingInstance.internalAdd(key, value); // increments size as well
				}
			}
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
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		iterator.accept(BinaryPersistence.get_long(bytes, BINARY_OFFSET_KEYS));
		iterator.accept(BinaryPersistence.get_long(bytes, BINARY_OFFSET_VALUES));
		BinaryCollectionHandling.iterateKeyValueEntriesReferences(bytes, BINARY_OFFSET_ELEMENTS, iterator);
	}

}
