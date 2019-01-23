package net.jadoth.collections;

import net.jadoth.X;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustomCollection;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceStoreHandler;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerFixedList
extends AbstractBinaryHandlerNativeCustomCollection<FixedList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final long BINARY_OFFSET_LIST = 0;



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<FixedList<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)FixedList.class;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerFixedList()
	{
		// binary layout definition
		super(
			typeWorkaround(),
			BinaryCollectionHandling.simpleArrayPseudoFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final FixedList<?>            instance,
		final long                    oid     ,
		final PersistenceStoreHandler handler
	)
	{
		final Object[] arrayInstance = instance.data;
		final long contentAddress = bytes.storeEntityHeader(
			BinaryPersistence.calculateReferenceListTotalBinaryLength(arrayInstance.length),
			this.typeId(),
			oid
		);
		BinaryPersistence.storeArrayContentAsList(contentAddress, handler, arrayInstance, 0, arrayInstance.length);
	}

	@Override
	public final FixedList<?> create(final Binary bytes)
	{
		return new FixedList<>(X.checkArrayRange(BinaryPersistence.getListElementCountReferences(bytes, 0)));
	}

	@Override
	public final void update(final Binary bytes, final FixedList<?> instance, final PersistenceLoadHandler builder)
	{
		final Object[] arrayInstance = instance.data;

		// length must be checked for consistency reasons
		BinaryCollectionHandling.validateArrayLength(arrayInstance, bytes, BINARY_OFFSET_LIST);

		final long binaryRefOffset = BinaryPersistence.binaryListElementsAddress(bytes, BINARY_OFFSET_LIST);
		for(int i = 0; i < arrayInstance.length; i++)
		{
			// bounds-check eliminated array setting has about equal performance as manual unsafe putting
			arrayInstance[i] = builder.lookupObject(XMemory.get_long(binaryRefOffset + i * BinaryPersistence.oidLength()));
		}
	}

	@Override
	public final void iterateInstanceReferences(final FixedList<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, instance.data, 0, instance.data.length);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		BinaryPersistence.iterateListElementReferences(bytes, BINARY_OFFSET_LIST, iterator);
	}

}
