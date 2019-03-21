package one.microstream.collections;

import java.lang.reflect.Field;

import one.microstream.equality.Equalator;
import one.microstream.hashing.HashEqualator;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollectionSizedArray;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.reflect.XReflect;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerEqBulkList
extends AbstractBinaryHandlerCustomCollectionSizedArray<EqBulkList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_EQUALATOR   =                           0; // one oid for equalator reference
	static final long BINARY_OFFSET_SIZED_ARRAY = Binary.objectIdByteLength(); // space offset for one oid

	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field FIELD_EQULATOR = XReflect.getInstanceFieldOfType(EqBulkList.class, Equalator.class);



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<EqBulkList<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)EqBulkList.class;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerEqBulkList(final PersistenceSizedArrayLengthController controller)
	{
		// binary layout definition
		super(
			typeWorkaround(),
			BinaryCollectionHandling.sizedArrayPseudoFields(
				pseudoField(HashEqualator.class, "hashEqualator")
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
		final long contentAddress = bytes.storeSizedArray(
			this.typeId()            ,
			objectId                 ,
			BINARY_OFFSET_SIZED_ARRAY,
			instance.data            ,
			instance.size            ,
			handler
		);

		// persist equalator and set the resulting oid at its binary place
		bytes.store_long(
			contentAddress + BINARY_OFFSET_EQUALATOR,
			handler.apply(instance.equalator)
		);
	}

	@Override
	public final EqBulkList<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		// this method only creates shallow instances, so hashEqualator gets set during update like other references.
		return new EqBulkList<>((Equalator<?>)null);
	}

	@Override
	public final void update(final Binary bytes, final EqBulkList<?> instance, final PersistenceLoadHandler handler)
	{
		// must clear to avoid memory leaks due to residual references beyond the new size in existing instances.
		instance.clear();
		
		// length must be checked for consistency reasons
		instance.ensureCapacity(this.determineArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY));

		instance.size = bytes.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY,
			instance.data            ,
			handler
		);

		// set equalator instance (must be done on memory-level due to final modifier. Little hacky, but okay)
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_EQULATOR),
			handler.lookupObject(bytes.get_long(BINARY_OFFSET_EQUALATOR))
		);
	}

	@Override
	public final void iterateInstanceReferences(final EqBulkList<?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.equalator);
		Persistence.iterateReferences(iterator, instance.data, 0, instance.size);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_EQUALATOR));
		bytes.iterateSizedArrayElementReferences(BINARY_OFFSET_SIZED_ARRAY, iterator);
	}

}
