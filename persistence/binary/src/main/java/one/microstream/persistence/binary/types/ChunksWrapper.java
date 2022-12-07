package one.microstream.persistence.binary.types;

/*-
 * #%L
 * microstream-persistence-binary
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.typing.XTypes;


public class ChunksWrapper extends Binary
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static ChunksWrapper New(final ByteBuffer... chunkDirectBuffers)
	{
		return new ChunksWrapper(chunkDirectBuffers);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final ByteBuffer[] buffers    ;
	private final long         totalLength;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	// internal constructor. Does not validate arguments!
	protected ChunksWrapper(final ByteBuffer[] chunks)
	{
		super();
		
		long totalLength = 0;
		for(int i = 0; i < chunks.length; i++)
		{
			// this is platform-independently platform-specific. No kidding. See code inside.
			if(!XTypes.isDirectByteBuffer(chunks[i]))
			{
				throw new IllegalArgumentException();
			}
			
			totalLength += chunks[i].position();
		}

		this.buffers     = chunks      ;
		this.totalLength = totalLength ;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	@Override
	public void iterateEntityData(final BinaryEntityDataReader reader)
	{
		final ByteBuffer[] buffers = this.buffers;
				
		for(int i = 0; i < buffers.length; i++)
		{
			reader.readBinaryEntities(buffers[i]);
		}
	}
	
	@Override
	public void iterateChannelChunks(final Consumer<? super Binary> logic)
	{
		logic.accept(this);
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
	public final boolean isEmpty()
	{
		return this.totalLength != 0;
	}
	
	@Override
	public final long totalLength()
	{
		return this.totalLength;
	}

	@Override
	public final ByteBuffer[] buffers()
	{
		return this.buffers;
	}

	@Override
	public final void storeEntityHeader(
		final long entityContentLength,
		final long entityTypeId       ,
		final long entityObjectId
	)
	{
		// optimization inheritance artifact: only storing chunk implementation can store
		throw new UnsupportedOperationException();
	}

	@Override
	public final long loadItemEntityContentAddress()
	{
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public final void clear()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public long iterateReferences(
		final BinaryReferenceTraverser[]  traversers,
		final PersistenceObjectIdAcceptor acceptor
	)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void mark()
	{
		final ByteBuffer[] buffers = this.buffers;
		
		for(int i = 0; i < buffers.length; i++)
		{
			buffers[i].mark();
		}
	}
	
	@Override
	public void reset()
	{
		final ByteBuffer[] buffers = this.buffers;
		
		for(int i = 0; i < buffers.length; i++)
		{
			buffers[i].reset();
		}
	}
			
}
