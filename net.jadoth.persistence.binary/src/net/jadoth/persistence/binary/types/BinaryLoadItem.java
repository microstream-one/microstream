package net.jadoth.persistence.binary.types;

import java.nio.ByteBuffer;

import net.jadoth.memory.XMemory;
import net.jadoth.persistence.types.PersistenceTypeHandler;

public final class BinaryLoadItem extends Binary
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryLoadItem SkipItem(final long objectId, final Object instance)
	{
		/*
		 * A little hacky, but worth it:
		 * Since BinaryLoadItem does not hold an oid value explicitely, but instead reads it from the entity header
		 * in the binary data, a skip item has to emulate/fake such data with the explicit skip oid written at a
		 * conforming offset. Skip items are hardly ever used, so the little detour and memory footprint overhead
		 * are well worth it if spares an additional explicit 8 byte long field for the millions and millions
		 * of common case entities.
		 */
		final ByteBuffer dbb = Binary.allocateEntityHeaderDirectBuffer();
		final long dbbAddress = XMemory.getDirectByteBufferAddress(dbb);
		Binary.setEntityHeaderRawValues(dbbAddress, 0, 0, objectId);
		
		// skip items do not require a type handler, only objectId, a fakeContentAddress and optional instance
		final BinaryLoadItem skipItem = new BinaryLoadItem(dbbAddress + dbb.capacity(), instance, null);
		
		// skip items will never use the helper instance for anything, since they are skip dummies.
		skipItem.setHelper(dbb);
		
		return skipItem;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	PersistenceTypeHandler<Binary, Object> handler;
	Object contextInstance, localInstance;
	BinaryLoadItem next, link;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	BinaryLoadItem(
		final long                                   entityContentAddress,
		final Object                                 contextInstance     ,
		final PersistenceTypeHandler<Binary, Object> handler
	)
	{
		super();
		this.address         = entityContentAddress;
		this.handler         = handler             ;
		this.contextInstance = contextInstance     ;
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
	public final void setLoadItemEntityContentAddress(final long entityContentAddress)
	{
		this.address = entityContentAddress;
	}
	
	@Override
	public final byte get_byte(final long offset)
	{
		return XMemory.get_byte(this.loadItemEntityContentAddress() + offset);
	}
	
	@Override
	public final boolean get_boolean(final long offset)
	{
		return XMemory.get_boolean(this.loadItemEntityContentAddress() + offset);
	}
	
	@Override
	public final short get_short(final long offset)
	{
		return XMemory.get_short(this.loadItemEntityContentAddress() + offset);
	}
	
	@Override
	public final char get_char(final long offset)
	{
		return XMemory.get_char(this.loadItemEntityContentAddress() + offset);
	}
	
	@Override
	public final int get_int(final long offset)
	{
		return XMemory.get_int(this.loadItemEntityContentAddress() + offset);
	}
	
	@Override
	public final float get_float(final long offset)
	{
		return XMemory.get_float(this.loadItemEntityContentAddress() + offset);
	}
	
	@Override
	public final long get_long(final long offset)
	{
		return XMemory.get_long(this.loadItemEntityContentAddress() + offset);
	}
	
	@Override
	public final double get_double(final long offset)
	{
		return XMemory.get_double(this.loadItemEntityContentAddress() + offset);
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