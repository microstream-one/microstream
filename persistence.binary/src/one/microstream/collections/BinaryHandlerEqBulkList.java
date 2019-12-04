package one.microstream.collections;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;

import one.microstream.equality.Equalator;
import one.microstream.hashing.HashEqualator;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomIterableSizedArray;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceStoreHandler;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerEqBulkList
extends AbstractBinaryHandlerCustomIterableSizedArray<EqBulkList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_EQUALATOR   =                                                     0;
	static final long BINARY_OFFSET_SIZED_ARRAY = BINARY_OFFSET_EQUALATOR + Binary.objectIdByteLength();

	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field FIELD_EQULATOR = getInstanceFieldOfType(EqBulkList.class, Equalator.class);



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<EqBulkList<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)EqBulkList.class;
	}
	
	public static BinaryHandlerEqBulkList New(final PersistenceSizedArrayLengthController controller)
	{
		return new BinaryHandlerEqBulkList(
			notNull(controller)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerEqBulkList(final PersistenceSizedArrayLengthController controller)
	{
		// binary layout definition
		super(
			handledType(),
			SizedArrayFields(
				CustomField(HashEqualator.class, "hashEqualator")
			),
			controller
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final EqBulkList<?>           instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// store elements as sized array, leave out space for equalator reference
		bytes.storeSizedArray(
			this.typeId()            ,
			objectId                 ,
			BINARY_OFFSET_SIZED_ARRAY,
			instance.data            ,
			instance.size            ,
			handler
		);

		// persist equalator and set the resulting oid at its binary place
		bytes.store_long(
			BINARY_OFFSET_EQUALATOR,
			handler.apply(instance.equalator)
		);
	}

	@Override
	public final EqBulkList<?> create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		// this method only creates shallow instances, so hashEqualator gets set during update like other references.
		return new EqBulkList<>((Equalator<?>)null);
	}

	@Override
	public final void update(
		final Binary                      bytes     ,
		final EqBulkList<?>               instance  ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		// must clear to avoid memory leaks due to residual references beyond the new size in existing instances.
		instance.clear();
		
		// length must be checked for consistency reasons
		instance.ensureCapacity(this.determineArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY));

		instance.size = bytes.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY,
			idResolver,
			instance.data
		);

		// set equalator instance (must be done on memory-level due to final modifier. Little hacky, but okay)
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_EQULATOR),
			idResolver.lookupObject(bytes.read_long(BINARY_OFFSET_EQUALATOR))
		);
	}

	@Override
	public final void iterateInstanceReferences(final EqBulkList<?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.equalator);
		Persistence.iterateReferences(iterator, instance.data, 0, instance.size);
	}

	@Override
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		iterator.acceptObjectId(bytes.read_long(BINARY_OFFSET_EQUALATOR));
		bytes.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}

}
