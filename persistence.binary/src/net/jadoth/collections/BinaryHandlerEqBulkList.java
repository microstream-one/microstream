package net.jadoth.collections;

import java.lang.reflect.Field;

import net.jadoth.equality.Equalator;
import net.jadoth.hashing.HashEqualator;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustomCollectionSizedArray;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceObjectIdAcceptor;
import net.jadoth.persistence.types.PersistenceSizedArrayLengthController;
import net.jadoth.persistence.types.PersistenceStoreHandler;
import net.jadoth.reflect.XReflect;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerEqBulkList
extends AbstractBinaryHandlerNativeCustomCollectionSizedArray<EqBulkList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_EQUALATOR   =                             0; // one oid for equalator reference
	static final long BINARY_OFFSET_SIZED_ARRAY = Binary.oidByteLength(); // space offset for one oid

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
		final long                    oid     ,
		final PersistenceStoreHandler handler
	)
	{
		// store elements as sized array, leave out space for equalator reference
		final long contentAddress = bytes.storeSizedArray(
			this.typeId()            ,
			oid                      ,
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
	public final EqBulkList<?> create(final Binary bytes)
	{
		// this method only creates shallow instances, so hashEqualator gets set during update like other references.
		return new EqBulkList<>((Equalator<?>)null);
	}

	@Override
	public final void update(final Binary bytes, final EqBulkList<?> instance, final PersistenceLoadHandler builder)
	{
		// length must be checked for consistency reasons
		instance.ensureCapacity(this.determineArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY));

		instance.size = bytes.updateSizedArrayObjectReferences(
			BINARY_OFFSET_SIZED_ARRAY,
			instance.data            ,
			builder
		);

		// set equalator instance (must be done on memory-level due to final modifier. Little hacky, but okay)
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_EQULATOR),
			builder.lookupObject(bytes.get_long(BINARY_OFFSET_EQUALATOR))
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
