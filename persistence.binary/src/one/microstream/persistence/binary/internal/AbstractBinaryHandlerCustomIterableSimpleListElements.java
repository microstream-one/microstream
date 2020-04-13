package one.microstream.persistence.binary.internal;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;


public abstract class AbstractBinaryHandlerCustomIterableSimpleListElements<T extends Iterable<?>>
extends AbstractBinaryHandlerCustomIterable<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_ELEMENTS = 0;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	protected static final long getElementCount(final Binary data)
	{
		return data.getListElementCountReferences(BINARY_OFFSET_ELEMENTS);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerCustomIterableSimpleListElements(final Class<T> type)
	{
		super(
			type,
			SimpleArrayFields()
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected long binaryOffsetElements()
	{
		return BINARY_OFFSET_ELEMENTS;
	}
	
	protected abstract long getElementCount(T instance);
	
	@Override
	public void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// store elements simply as array binary form
		data.storeIterableAsList(
			this.typeId()                 ,
			objectId                      ,
			this.binaryOffsetElements()   ,
			instance                      ,
			this.getElementCount(instance),
			handler
		);
	}

	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		data.iterateListElementReferences(this.binaryOffsetElements(), iterator);
	}
	
}
