package net.jadoth.persistence.binary.types;

import java.nio.ByteBuffer;

import net.jadoth.persistence.types.PersistenceTypeHandler;

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
	public final void setLoadItemEntityContentAddress(final long entityContentAddress)
	{
		this.address = entityContentAddress;
	}
	

		
	/**
	 * In rare cases (legacy type mapping), a direct byte buffer must be "anchored" in order to not get gc-collected
	 * and cause its memory to be deallocated. Anchoring means it just has to be referenced by anything that lives
	 * until the end of the entity loading/building process. It never has to be dereferenced again.
	 * In order to not need another fixed field, which would needlessly occupy memory for EVERY entity in almost every
	 * case, a "helper anchor" is used: a nifty instance that is clamped in between the actual load item and the actual
	 * helper instance.
	 * 
	 * @author TM
	 *
	 */
	static final class HelperAnchor
	{
		final Object anchorSubject;
		      Object actualHelper;
		
		HelperAnchor(final Object anchorSubject, final Object actualHelper)
		{
			super();
			this.anchorSubject = anchorSubject;
			this.actualHelper  = actualHelper;
		}
		
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