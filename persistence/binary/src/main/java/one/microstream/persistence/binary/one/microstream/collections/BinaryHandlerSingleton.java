package one.microstream.persistence.binary.one.microstream.collections;

import one.microstream.collections.Singleton;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;


// (28.12.2019 TM)NOTE: the sole purpose of this implementation is to provide the explicit method #getReferenceObjectId
public final class BinaryHandlerSingleton extends AbstractBinaryHandlerCustomCollection<Singleton<Object>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	private static long binaryOffsetReference()
	{
		return 0L;
	}

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<Singleton<Object>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)Singleton.class;
	}
	
	public static long getReferenceObjectId(final Binary data)
	{
		return data.read_long(binaryOffsetReference());
	}
	
	public static BinaryHandlerSingleton New()
	{
		return new BinaryHandlerSingleton();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerSingleton()
	{
		super(
			handledType(),
			CustomFields(
				CustomField(Object.class, "element")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(
		final Binary                          data    ,
		final Singleton<Object>               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(Binary.referenceBinaryLength(1), this.typeId(), objectId);
		final long referenceObjectId = handler.apply(instance.get());
		data.store_long(referenceObjectId);
	}

	@Override
	public Singleton<Object> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return Singleton.New(null);
	}

	@Override
	public void updateState(final Binary data, final Singleton<Object> instance, final PersistenceLoadHandler handler)
	{
//		@SuppressWarnings("unchecked")
//		final Singleton<Object> casted = instance;
		
		final long refObjectId = getReferenceObjectId(data);
		final Object reference = handler.lookupObject(refObjectId);
		XCollectionsInternals.setElement(instance, reference);
	}
	
	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		final long refObjectId = getReferenceObjectId(data);
		iterator.acceptObjectId(refObjectId);
	}
	
	@Override
	public void iterateInstanceReferences(final Singleton<Object> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.get());
	}

	@Override
	public final boolean hasPersistedVariableLength()
	{
		return false;
	}
	
	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

}
