package net.jadoth.collections;

import net.jadoth.functional._longProcedure;
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
public final class BinaryHandlerLimitList
extends AbstractBinaryHandlerNativeCustomCollection<LimitList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_SIZED_ARRAY = 0; // binary form is 100% just a sized array, so offset 0



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<LimitList<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)LimitList.class;
	}

	private static int getBuildItemArrayLength(final Binary bytes)
	{
		return BinaryCollectionHandling.getSizedArrayLength(bytes, BINARY_OFFSET_SIZED_ARRAY);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerLimitList()
	{
		// binary layout definition
		super(
			typeWorkaround(),
			BinaryCollectionHandling.sizedArrayPseudoFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final LimitList<?>            instance,
		final long                    oid     ,
		final PersistenceStoreHandler handler
	)
	{
		BinaryCollectionHandling.storeSizedArray(
			bytes                    ,
			this.typeId()            ,
			oid                      ,
			BINARY_OFFSET_SIZED_ARRAY,
			instance.data            ,
			instance.size            ,
			handler
		);
	}

	@Override
	public final LimitList<?> create(final Binary bytes)
	{
		return new LimitList<>(getBuildItemArrayLength(bytes));
	}

	@Override
	public final void update(final Binary bytes, final LimitList<?> instance, final PersistenceLoadHandler builder)
	{
		// length must be checked for consistency reasons
		instance.ensureCapacity(getBuildItemArrayLength(bytes));
		instance.size = BinaryCollectionHandling.updateSizedArrayObjectReferences(
			bytes                    ,
			BINARY_OFFSET_SIZED_ARRAY,
			instance.data            ,
			builder
		);
	}

	@Override
	public final void iterateInstanceReferences(final LimitList<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, instance.data, 0, instance.size);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		BinaryCollectionHandling.iterateSizedArrayElementReferences(bytes, BINARY_OFFSET_SIZED_ARRAY, iterator);
	}

}
