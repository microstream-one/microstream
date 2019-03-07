package one.microstream.persistence.binary.types;

import java.nio.ByteBuffer;

import one.microstream.persistence.types.PersistenceTypeHandler;

public class BinaryLoadItem extends Binary
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	PersistenceTypeHandler<Binary, Object> handler;
	Object contextInstance, localInstance;
	BinaryLoadItem next, link;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	BinaryLoadItem(final long entityContentAddress)
	{
		super();
		this.address = entityContentAddress;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final Binary channelChunk(final int channelIndex)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final int channelCount()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void iterateEntityData(final BinaryEntityDataReader reader)
	{
		// technically, the single data set could be iterated, but designwise, it's not the task, here.
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Some binary entries serve as a skip entry, so that an entry for a particular object id already exists.
	 * Naturally, those entries don't have data then, which must be checked (be checkable) later on.
	 *
	 * @return whether this instances carries (actually "knows") binary build data or not.
	 */
	public final boolean hasData()
	{
		/*
		 * since all proper build items are validated to have a non-null handler,
		 * a null handler can be safely used to indicate skip items, i.e. no data.
		 * 
		 */
		return this.handler != null;
	}

	@Override
	public final long loadItemEntityContentAddress()
	{
		return this.address;
	}

	@Override
	public final long loadItemEntityAddress()
	{
		return entityAddressFromContentAddress(this.address);
	}
	
	@Override
	public final void modifyLoadItem(
		final long entityContentAddress,
		final long entityTotalLength   ,
		final long entityTypeId        ,
		final long entityObjectId
	)
	{
		this.address = entityContentAddress;
		this.internalStoreEntityHeader(
			this.loadItemEntityAddress(),
			entityTotalLength,
			entityTypeId,
			entityObjectId
		);
	}
	
	@Override
	public String toString()
	{
		return "LoadItem OID=" + this.getBuildItemObjectId()
			+ (this.handler == null
				? "[no handler]"
				: ", Type=" + this.handler.typeId() + " " + this.handler.typeName())
		;
	}
			
				
	@Override
	public final long storeEntityHeader(
		final long entityContentLength,
		final long entityTypeId       ,
		final long entityObjectId
	)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final ByteBuffer[] buffers()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean isEmpty()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public final long totalLength()
	{
		throw new UnsupportedOperationException();
	}

}