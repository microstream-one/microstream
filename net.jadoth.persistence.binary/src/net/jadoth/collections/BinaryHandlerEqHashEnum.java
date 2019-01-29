package net.jadoth.collections;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.functional._longProcedure;
import net.jadoth.hashing.HashEqualator;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustomCollection;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceStoreHandler;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberPseudoField;
import net.jadoth.reflect.XReflect;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerEqHashEnum
extends AbstractBinaryHandlerNativeCustomCollection<EqHashEnum<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long
		BINARY_OFFSET_EQUALATOR    =                                                              0, // oid for eqltr ref
		BINARY_OFFSET_HASH_DENSITY = BINARY_OFFSET_EQUALATOR    + BinaryPersistence.oidByteLength(), // offset for 1 oid
		BINARY_OFFSET_ELEMENTS     = BINARY_OFFSET_HASH_DENSITY + XMemory.byteSize_float()           // offset for 1 float
;
	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field FIELD_EQULATOR = XReflect.getInstanceFieldOfType(EqHashEnum.class, HashEqualator.class);



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

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
		return BinaryPersistence.get_float(bytes, BINARY_OFFSET_HASH_DENSITY);
	}

	public static final void staticStore(
		final Binary          bytes    ,
		final EqHashEnum<?>   instance ,
		final long            tid      ,
		final long            oid      ,
		final PersistenceFunction persister
	)
	{
		// store elements simply as array binary form
		final long contentAddress = bytes.storeSizedIterableAsList(
			tid                   ,
			oid                   ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			persister
		);

		// persist hashEqualator and set the resulting oid at its binary place (first header value)
		XMemory.set_long(
			contentAddress + BINARY_OFFSET_EQUALATOR,
			persister.apply(instance.hashEqualator)
		);

		// store hash density as second header value
		XMemory.set_float(
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
		@SuppressWarnings("unchecked") // necessary because this handler operates on a generic technical level
		final EqHashEnum<Object> collectingInstance = (EqHashEnum<Object>)instance;

		// length must be checked for consistency reasons
		instance.ensureCapacity(getBuildItemElementCount(bytes));

		// set equalator instance (must be done on memory-level due to final modifier. Little hacky, but okay)
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_EQULATOR),
			handler.lookupObject(BinaryPersistence.get_long(bytes, BINARY_OFFSET_EQUALATOR))
		);

		// collect elements AFTER hashEqualator has been set because it is used in it
		instance.size = BinaryPersistence.collectListObjectReferences(
			bytes                 ,
			BINARY_OFFSET_ELEMENTS,
			handler               ,
			new Consumer<Object>()
			{
				@Override
				public void accept(final Object e)
				{
					// unhashed because element instances are potentially not populated with data yet. see complete()
					collectingInstance.internalCollectUnhashed(e);
				}
			}
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

	public static final void staticIteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		iterator.accept(BinaryPersistence.get_long(bytes, BINARY_OFFSET_EQUALATOR));
		BinaryPersistence.iterateListElementReferences(bytes, BINARY_OFFSET_ELEMENTS, iterator);
	}

	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> pseudoFields()
	{
		return BinaryCollectionHandling.elementsPseudoFields(
			pseudoField(HashEqualator.class, "hashEqualator"),
			pseudoField(float.class, "hashDensity")
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

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
		final long                    oid     ,
		final PersistenceStoreHandler handler
	)
	{
		staticStore(bytes, instance, this.typeId(), oid, handler);
	}

	@Override
	public final EqHashEnum<?> create(final Binary bytes)
	{
		return staticCreate(bytes);
	}

	@Override
	public final void update(final Binary bytes, final EqHashEnum<?> instance, final PersistenceLoadHandler builder)
	{
		staticUpdate(bytes, instance, builder);
	}

	@Override
	public final void complete(final Binary medium, final EqHashEnum<?> instance, final PersistenceLoadHandler builder)
	{
		staticComplete(medium, instance);
	}

	@Override
	public final void iterateInstanceReferences(final EqHashEnum<?> instance, final PersistenceFunction iterator)
	{
		staticIterateInstanceReferences(instance, iterator);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		staticIteratePersistedReferences(bytes, iterator);
	}

}
