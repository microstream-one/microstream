package net.jadoth.collections;

import java.lang.reflect.Field;

import net.jadoth.functional._longProcedure;
import net.jadoth.hash.HashEqualator;
import net.jadoth.memory.Memory;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustomCollection;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.swizzling.types.PersistenceStoreFunction;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;
import net.jadoth.util.Equalator;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerEqBulkList
extends AbstractBinaryHandlerNativeCustomCollection<EqBulkList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_EQUALATOR   =                             0; // one oid for equalator reference
	static final long BINARY_OFFSET_SIZED_ARRAY = BinaryPersistence.oidLength(); // space offset for one oid

	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field FIELD_EQULATOR = JadothReflect.getInstanceFieldOfType(EqBulkList.class, Equalator.class);



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<EqBulkList<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)EqBulkList.class;
	}

	private static int getBuildItemElementCount(final Binary bytes)
	{
		return BinaryCollectionHandling.getSizedArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerEqBulkList(final long typeId)
	{
		// binary layout definition
		super(
			typeId,
			typeWorkaround(),
			BinaryCollectionHandling.sizedArrayPseudoFields(
				pseudoField(HashEqualator.class, "hashEqualator")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary          bytes    ,
		final EqBulkList<?>   instance ,
		final long            oid      ,
		final PersistenceStoreFunction linker
	)
	{
		// store elements as sized array, leave out space for equalator reference
		final long contentAddress = BinaryCollectionHandling.storeSizedArray(
			bytes                    ,
			this.typeId()            ,
			oid                      ,
			BINARY_OFFSET_SIZED_ARRAY,
			instance.data            ,
			instance.size            ,
			linker
		);

		// persist equalator and set the resulting oid at its binary place
		Memory.set_long(
			contentAddress + BINARY_OFFSET_EQUALATOR,
			linker.apply(instance.equalator)
		);
	}

	@Override
	public final EqBulkList<?> create(final Binary bytes)
	{
		// this method only creates shallow instances, so hashEqualator gets set during update like other references.
		return new EqBulkList<>(null, BinaryCollectionHandling.getSizedArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY));
	}

	@Override
	public final void update(final Binary bytes, final EqBulkList<?> instance, final SwizzleBuildLinker builder)
	{
		// length must be checked for consistency reasons
		instance.ensureCapacity(getBuildItemElementCount(bytes));

		instance.size = BinaryCollectionHandling.updateSizedArrayObjectReferences(
			bytes                    ,
			BINARY_OFFSET_SIZED_ARRAY,
			instance.data            ,
			builder
		);

		// set equalator instance (must be done on memory-level due to final modifier. Little hacky, but okay)
		Memory.setObject(
			instance,
			Memory.objectFieldOffset(FIELD_EQULATOR),
			builder.lookupObject(BinaryPersistence.get_long(bytes, BINARY_OFFSET_EQUALATOR))
		);
	}

	@Override
	public final void iterateInstanceReferences(final EqBulkList<?> instance, final SwizzleFunction iterator)
	{
		iterator.apply(instance.equalator);
		Swizzle.iterateReferences(iterator, instance.data, 0, instance.size);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		iterator.accept(BinaryPersistence.get_long(bytes, BINARY_OFFSET_EQUALATOR));
		BinaryCollectionHandling.iterateSizedArrayElementReferences(bytes, BINARY_OFFSET_SIZED_ARRAY, iterator);
	}

}
