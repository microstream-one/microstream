package net.jadoth.collections;

import net.jadoth.X;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustomCollection;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceObjectIdAcceptor;
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
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		bytes.storeReferences(
			this.typeId(),
			objectId     ,
			0            ,
			handler      ,
			instance.data
		);
	}

	@Override
	public final FixedList<?> create(final Binary bytes)
	{
		return new FixedList<>(X.checkArrayRange(bytes.getListElementCountReferences(0)));
	}

	@Override
	public final void update(final Binary bytes, final FixedList<?> instance, final PersistenceLoadHandler builder)
	{
		final Object[] arrayInstance = instance.data;

		// length must be checked for consistency reasons
		bytes.validateArrayLength(arrayInstance, BINARY_OFFSET_LIST);

		final long binaryRefOffset = bytes.binaryListElementsAddress(BINARY_OFFSET_LIST);
		for(int i = 0; i < arrayInstance.length; i++)
		{
			// bounds-check eliminated array setting has about equal performance as manual unsafe putting
			arrayInstance[i] = builder.lookupObject(XMemory.get_long(binaryRefOffset + i * Binary.oidByteLength()));
		}
	}

	@Override
	public final void iterateInstanceReferences(final FixedList<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, instance.data, 0, instance.data.length);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_LIST, iterator);
	}

}
