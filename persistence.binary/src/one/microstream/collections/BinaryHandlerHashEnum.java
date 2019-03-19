package one.microstream.collections;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerHashEnum
extends AbstractBinaryHandlerCustomCollection<HashEnum<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_HASH_DENSITY =           0;
	static final long BINARY_OFFSET_ELEMENTS     = Float.BYTES; // one float offset to sized array



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<HashEnum<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)HashEnum.class;
	}

	private static long getBuildItemElementCount(final Binary bytes)
	{
		return bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS);
	}

	private static float getBuildItemHashDensity(final Binary bytes)
	{
		return bytes.get_float(BINARY_OFFSET_HASH_DENSITY);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerHashEnum()
	{
		// binary layout definition
		super(
			typeWorkaround(),
			BinaryCollectionHandling.elementsPseudoFields(
				pseudoField(float.class, "hashDensity")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final HashEnum<?>             instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// store elements simply as array binary form
		final long contentAddress = bytes.storeSizedIterableAsList(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);

		// store hash density as (sole) header value
		bytes.store_float(
			contentAddress + BINARY_OFFSET_HASH_DENSITY,
			instance.hashDensity
		);
	}

	@Override
	public final HashEnum<?> create(final Binary bytes)
	{
		return HashEnum.NewCustom(
			getBuildItemElementCount(bytes),
			getBuildItemHashDensity(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final HashEnum<?> instance, final PersistenceLoadHandler builder)
	{
		@SuppressWarnings("unchecked") // necessary because this handler operates on a generic technical level
		final HashEnum<Object> collectingInstance = (HashEnum<Object>)instance;

		// length must be checked for consistency reasons
		instance.ensureCapacity(getBuildItemElementCount(bytes));

		instance.size = bytes.collectListObjectReferences(
			BINARY_OFFSET_ELEMENTS,
			builder               ,
			collectingInstance::add
		);
		// note: hashDensity has already been set at creation time (shallow primitive value)
	}

	@Override
	public final void iterateInstanceReferences(final HashEnum<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, instance);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}

}
