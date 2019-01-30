package net.jadoth.collections;

import net.jadoth.X;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustomCollection;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceStoreHandler;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerConstHashEnum
extends AbstractBinaryHandlerNativeCustomCollection<ConstHashEnum<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_HASH_DENSITY =                        0;
	static final long BINARY_OFFSET_ELEMENTS     = XMemory.byteSize_float(); // one float offset to sized array



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<ConstHashEnum<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)ConstHashEnum.class;
	}

	private static int getBuildItemElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}

	private static float getBuildItemHashDensity(final Binary bytes)
	{
		return bytes.get_float(BINARY_OFFSET_HASH_DENSITY);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerConstHashEnum()
	{
		// binary layout definition
		super(
			typeWorkaround(),
			BinaryCollectionHandling.elementsPseudoFields(pseudoField(float.class, "hashDensity"))
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final ConstHashEnum<?>        instance,
		final long                    oid     ,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		final long contentAddress = bytes.storeSizedIterableAsList(
			this.typeId()         ,
			oid                   ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);

		// store hash density as (sole) header value
		XMemory.set_float(contentAddress, instance.hashDensity);
	}

	@Override
	public final ConstHashEnum<?> create(final Binary bytes)
	{
		return ConstHashEnum.NewCustom(
			getBuildItemElementCount(bytes),
			getBuildItemHashDensity(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final ConstHashEnum<?> instance, final PersistenceLoadHandler builder)
	{
		@SuppressWarnings("unchecked") // necessary because this handler operates on a generic technical level
		final ConstHashEnum<Object> casted = (ConstHashEnum<Object>)instance;

		// validate to the best of possibilities
		if(instance.size != 0)
		{
			throw new IllegalStateException(); // (26.10.2013)EXCP: proper exception
		}

		instance.size = bytes.collectListObjectReferences(
			BINARY_OFFSET_ELEMENTS,
			builder               ,
			casted::internalAdd
		);
		// note: hashDensity has already been set at creation time (shallow primitive value)
	}

	@Override
	public final void iterateInstanceReferences(final ConstHashEnum<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, instance);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}

}
