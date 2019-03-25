package one.microstream.collections;

import java.lang.reflect.Field;

import one.microstream.X;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.hashing.HashEqualator;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoField;
import one.microstream.reflect.XReflect;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerEqHashEnum
extends AbstractBinaryHandlerCustomCollection<EqHashEnum<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long
		BINARY_OFFSET_EQUALATOR    =                                                   0, // oid for eqltr ref
		BINARY_OFFSET_HASH_DENSITY = BINARY_OFFSET_EQUALATOR    + Binary.objectIdByteLength(), // offset for 1 oid
		BINARY_OFFSET_ELEMENTS     = BINARY_OFFSET_HASH_DENSITY + Float.BYTES             // offset for 1 float
;
	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field FIELD_EQULATOR = XReflect.getInstanceFieldOfType(EqHashEnum.class, HashEqualator.class);



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<EqHashEnum<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)EqHashEnum.class;
	}

	private static int getBuildItemElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}

	private static float getBuildItemHashDensity(final Binary bytes)
	{
		return bytes.get_float(BINARY_OFFSET_HASH_DENSITY);
	}

	public static final void staticStore(
		final Binary              bytes    ,
		final EqHashEnum<?>       instance ,
		final long                typeId   ,
		final long                objectId ,
		final PersistenceFunction persister
	)
	{
		// store elements simply as array binary form
		final long contentAddress = bytes.storeIterableAsList(
			typeId                ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			persister
		);

		// persist hashEqualator and set the resulting oid at its binary place (first header value)
		bytes.store_long(
			contentAddress + BINARY_OFFSET_EQUALATOR,
			persister.apply(instance.hashEqualator)
		);

		// store hash density as second header value
		bytes.store_float(
			contentAddress + BINARY_OFFSET_HASH_DENSITY,
			instance.hashDensity
		);
	}

	public static final EqHashEnum<?> staticCreate(final Binary bytes)
	{
		return EqHashEnum.NewCustom(
			getBuildItemElementCount(bytes),
			getBuildItemHashDensity(bytes)
		);
	}

	public static final void staticUpdate(
		final Binary                 bytes   ,
		final EqHashEnum<?>          instance,
		final PersistenceLoadHandler handler
	)
	{
		// must clear to ensure consistency
		instance.clear();
		
		@SuppressWarnings("unchecked") // necessary because this handler operates on a generic technical level
		final EqHashEnum<Object> casted = (EqHashEnum<Object>)instance;

		// length must be checked for consistency reasons
		instance.ensureCapacity(getBuildItemElementCount(bytes));

		// set equalator instance (must be done on memory-level due to final modifier. Little hacky, but okay)
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_EQULATOR),
			handler.lookupObject(bytes.get_long(BINARY_OFFSET_EQUALATOR))
		);

		// collect elements AFTER hashEqualator has been set because it is used in it
		instance.size = bytes.collectListObjectReferences(
			BINARY_OFFSET_ELEMENTS,
			handler               ,
			casted::internalCollectUnhashed
		);
		// note: hashDensity has already been set at creation time (shallow primitive value)
	}

	public static final void staticComplete(final Binary medium, final EqHashEnum<?> instance)
	{
		// rehash all previously unhashed collected elements
		instance.rehash();
	}

	public static final void staticIterateInstanceReferences(
		final EqHashEnum<?>   instance,
		final PersistenceFunction iterator
	)
	{
		iterator.apply(instance.hashEqualator);
		Persistence.iterateReferences(iterator, instance);
	}

	public static final void staticIteratePersistedReferences(
		final Binary                      bytes   ,
		final PersistenceObjectIdAcceptor iterator
	)
	{
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_EQUALATOR));
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}

	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> pseudoFields()
	{
		return BinaryCollectionHandling.simpleArrayPseudoFields(
			pseudoField(HashEqualator.class, "hashEqualator"),
			pseudoField(float.class, "hashDensity")
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerEqHashEnum()
	{
		// binary layout definition
		super(
			typeWorkaround(),
			pseudoFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final EqHashEnum<?>           instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		staticStore(bytes, instance, this.typeId(), objectId, handler);
	}

	@Override
	public final EqHashEnum<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return staticCreate(bytes);
	}

	@Override
	public final void update(final Binary bytes, final EqHashEnum<?> instance, final PersistenceLoadHandler handler)
	{
		staticUpdate(bytes, instance, handler);
	}

	@Override
	public final void complete(final Binary medium, final EqHashEnum<?> instance, final PersistenceLoadHandler handler)
	{
		staticComplete(medium, instance);
	}

	@Override
	public final void iterateInstanceReferences(final EqHashEnum<?> instance, final PersistenceFunction iterator)
	{
		staticIterateInstanceReferences(instance, iterator);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		staticIteratePersistedReferences(bytes, iterator);
	}

}
