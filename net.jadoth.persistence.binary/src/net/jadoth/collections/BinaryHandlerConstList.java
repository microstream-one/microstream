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
public final class BinaryHandlerConstList
extends AbstractBinaryHandlerNativeCustomCollection<ConstList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final long BINARY_OFFSET_LIST = 0; // binary form is 100% just a simple list, so offset 0



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<ConstList<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)ConstList.class;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerConstList()
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
		final ConstList<?>            instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		bytes.storeArray(
			this.typeId(),
			objectId     ,
			0            ,
			handler      ,
			instance.data
		);
	}

	@Override
	public final ConstList<?> create(final Binary bytes)
	{
		return ConstList.New(X.checkArrayRange(bytes.getListElementCountReferences(0)));
	}

	@Override
	public final void update(final Binary bytes, final ConstList<?> instance, final PersistenceLoadHandler builder)
	{
		final Object[] arrayInstance = instance.data;

		// length must be checked for consistency reasons
		bytes.validateArrayLength(arrayInstance, BINARY_OFFSET_LIST);

		final long binaryRefOffset = bytes.binaryListElementsAddressRelative(BINARY_OFFSET_LIST);
		for(int i = 0; i < arrayInstance.length; i++)
		{
			// bounds-check eliminated array setting has about equal performance as manual unsafe putting
			arrayInstance[i] = builder.lookupObject(XMemory.get_long(binaryRefOffset + i * Binary.oidByteLength()));
		}
	}

	@Override
	public final void iterateInstanceReferences(final ConstList<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, instance.data, 0, instance.data.length);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_LIST, iterator);
	}

}
