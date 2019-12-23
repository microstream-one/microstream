package one.microstream.persistence.binary.types;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceTypeHandler;

public class BinaryLoadItem extends Binary
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	PersistenceTypeHandler<Binary, Object> handler;
	Object existingInstance, createdInstance;
	BinaryLoadItem next, link;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryLoadItem(final long entityContentAddress)
	{
		super();
		this.address = entityContentAddress;
	}
	
	BinaryLoadItem(final long objectId, final Object existingInstance)
	{
		this(-objectId);
		this.existingInstance = existingInstance;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
		
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
		 */
		return this.handler != null;
	}

	@Override
	final long loadItemEntityContentAddress()
	{
		return this.address;
	}
	
	@Override
	public final long iterateReferences(
		final BinaryReferenceTraverser[]  traversers,
		final PersistenceObjectIdAcceptor acceptor
	)
	{
		if(!this.isProper())
		{
			throw new Error("Improper items cannot iterate references.");
		}
		
		long a = this.address;
		for(int i = 0; i < traversers.length; i++)
		{
			a = traversers[i].apply(a, acceptor);
		}
		
		return a;
	}
	
	@Override
	public final void modifyLoadItem(
		final ByteBuffer directByteBuffer ,
		final long       offset           ,
		final long       entityTotalLength,
		final long       entityTypeId     ,
		final long       entityObjectId
	)
	{
		final long entityAddress = this.calculateAddress(directByteBuffer, offset);
		this.address = toEntityContentOffset(entityAddress);
		this.storeEntityHeaderToAddress(entityAddress, entityTotalLength, entityTypeId, entityObjectId);
	}
	
	@Override
	public String toString()
	{
		return "LoadItem OID=" + (this.isDummyItem() ? " [Dummy]" : Long.toString(this.getBuildItemObjectId()))
			+ (this.handler == null
				? " [no handler]"
				: ", Type=" + this.handler.typeId() + " " + this.handler.typeName())
		;
	}
	
			
				
	@Override
	public final void storeEntityHeader(
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
	
	@Override
	public void iterateChannelChunks(final Consumer<? super Binary> logic)
	{
		// technically, the single data set could be iterated, but designwise, it's not the task, here.
		throw new UnsupportedOperationException();
	}

}
