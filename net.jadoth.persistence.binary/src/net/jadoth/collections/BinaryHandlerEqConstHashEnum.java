package net.jadoth.collections;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import net.jadoth.X;
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
import net.jadoth.reflect.XReflect;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerEqConstHashEnum
extends AbstractBinaryHandlerNativeCustomCollection<EqConstHashEnum<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	// one oid for equalator reference
	static final long BINARY_OFFSET_EQUALATOR    =                                                          0;
	// space offset for one oid
	static final long BINARY_OFFSET_HASH_DENSITY = BINARY_OFFSET_EQUALATOR    + BinaryPersistence.oidLength();
	// one float offset to sized array
	static final long BINARY_OFFSET_ELEMENTS     = BINARY_OFFSET_HASH_DENSITY + XMemory.byteSize_float()      ;

	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field FIELD_EQULATOR = XReflect.getInstanceFieldOfType(EqConstHashEnum.class, HashEqualator.class);



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<EqConstHashEnum<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)EqConstHashEnum.class;
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

	public BinaryHandlerEqConstHashEnum()
	{
		// binary layout definition
		super(
			typeWorkaround(),
			BinaryCollectionHandling.elementsPseudoFields(
				pseudoField(HashEqualator.class, "hashEqualator"),
				pseudoField(float.class        , "hashDensity"  )
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final EqConstHashEnum<?>      instance,
		final long                    oid     ,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		final long contentAddress = BinaryCollectionHandling.storeSizedIterableAsList(
			bytes                 ,
			this.typeId()         ,
			oid                   ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);

		// persist hashEqualator and set the resulting oid at its binary place (first header value)
		XMemory.set_long(
			contentAddress + BINARY_OFFSET_EQUALATOR,
			handler.apply(instance.hashEqualator)
		);

		// store hash density as second header value
		XMemory.set_float(
			contentAddress + BINARY_OFFSET_HASH_DENSITY,
			instance.hashDensity
		);
	}

	@Override
	public final EqConstHashEnum<?> create(final Binary bytes)
	{
		return EqConstHashEnum.New(
			getBuildItemElementCount(bytes),
			getBuildItemHashDensity(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final EqConstHashEnum<?> instance, final PersistenceLoadHandler builder)
	{
		@SuppressWarnings("unchecked") // necessary because this handler operates on a generic technical level
		final EqConstHashEnum<Object> collectingInstance = (EqConstHashEnum<Object>)instance;

		// length must be checked for consistency reasons
		if(instance.size != 0)
		{
			throw new IllegalStateException(); // (28.10.2013 TM)EXCP: proper exception
		}

		// set equalator instance (must be done on memory-level due to final modifier. Little hacky, but okay)
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_EQULATOR),
			builder.lookupObject(BinaryPersistence.get_long(bytes, BINARY_OFFSET_EQUALATOR))
		);

		// collect elements AFTER hashEqualator has been set because it is used in it
		instance.size = BinaryPersistence.collectListObjectReferences(
			bytes                 ,
			BINARY_OFFSET_ELEMENTS,
			builder               ,
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

	@Override
	public final void complete(final Binary medium, final EqConstHashEnum<?> instance, final PersistenceLoadHandler builder)
	{
		// rehash all previously unhashed collected elements
		instance.internalRehash();
	}

	@Override
	public final void iterateInstanceReferences(final EqConstHashEnum<?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.hashEqualator);
		Persistence.iterateReferences(iterator, instance);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		iterator.accept(BinaryPersistence.get_long(bytes, BINARY_OFFSET_EQUALATOR));
		BinaryPersistence.iterateListElementReferences(bytes, BINARY_OFFSET_ELEMENTS, iterator);
	}

}
