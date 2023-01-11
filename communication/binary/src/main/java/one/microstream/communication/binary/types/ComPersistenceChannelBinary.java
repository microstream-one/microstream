package one.microstream.communication.binary.types;

/*-
 * #%L
 * microstream-communication-binary
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

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.com.ComException;
import one.microstream.communication.types.ComConnection;
import one.microstream.communication.types.ComPersistenceChannel;
import one.microstream.memory.XMemory;
import one.microstream.meta.XDebug;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.ChunksWrapper;
import one.microstream.persistence.binary.types.ChunksWrapperByteReversing;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.ByteOrderTargeting;
import one.microstream.persistence.types.PersistenceWriteController;
import one.microstream.util.BufferSizeProvider;


public interface ComPersistenceChannelBinary<C> extends ComPersistenceChannel<C, Binary>
{
	public static ComPersistenceChannelBinary.Default New(
		final ComConnection              connection           ,
		final BufferSizeProvider         bufferSizeProvider,
		final ByteOrderTargeting<?>      byteOrderTargeting,
		final PersistenceWriteController writeController
	)
	{
		return new ComPersistenceChannelBinary.Default(
			notNull(connection)           ,
			notNull(bufferSizeProvider),
			notNull(byteOrderTargeting),
			notNull(writeController)
		);
	}
	
	public abstract class Abstract<C>
	extends ComPersistenceChannel.Abstract<C, Binary>
	implements ComPersistenceChannelBinary<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final BufferSizeProvider bufferSizeProvider;
		private       ByteBuffer         defaultBufferRead;
		private       ByteBuffer         defaultBufferWrite;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Abstract(final C channel, final BufferSizeProvider bufferSizeProvider)
		{
			super(channel);
			this.bufferSizeProvider = bufferSizeProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected ByteBuffer ensureDefaultBufferRead()
		{
			if(this.defaultBufferRead == null)
			{
				this.defaultBufferRead = XMemory.allocateDirectNative(
					this.bufferSizeProvider.provideBufferSize()
				);
			}
			
			return this.defaultBufferRead;
		}
		
		protected ByteBuffer ensureDefaultBufferWrite()
		{
			if(this.defaultBufferWrite == null)
			{
				this.defaultBufferWrite = XMemory.allocateDirectNative(
					this.bufferSizeProvider.provideBufferSize()
				);
			}
			
			return this.defaultBufferWrite;
		}
		
	}
	

	
	public final class Default extends ComPersistenceChannelBinary.Abstract<ComConnection>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final ByteOrderTargeting<?>      byteOrderTargeting;
		private final PersistenceWriteController writeController   ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final ComConnection              connection           ,
			final BufferSizeProvider         bufferSizeProvider,
			final ByteOrderTargeting<?>      byteOrderTargeting,
			final PersistenceWriteController writeController
		)
		{
			super(connection, bufferSizeProvider);
			this.byteOrderTargeting = byteOrderTargeting;
			this.writeController    = writeController   ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private boolean switchByteOrder()
		{
			return this.byteOrderTargeting.isByteOrderMismatch();
		}
		
		@Override
		protected XGettingCollection<? extends Binary> internalRead(final ComConnection connection)
			throws PersistenceExceptionTransfer
		{
			final ByteBuffer defaultBuffer = this.ensureDefaultBufferRead();
			

			ByteBuffer filledContentBuffer;
			try
			{
				filledContentBuffer = ComBinary.readChunk(
					connection,
					defaultBuffer,
					this.switchByteOrder()
				);
			}
			catch(final ComException e)
			{
				/* (13.11.2018 TM)TODO: shouldn't the content bytes be siphoned off, here?
				 * But what if the content was incomplete (or the specified content length too long) and
				 * the sockets already reads into the next chunk?
				 * Network communication can encounter all kinds of problems and it is not clear
				 * what guarantees the nio and underlying layers already make.
				 */
				throw new PersistenceExceptionTransfer(e);
			}
			
			final ChunksWrapper chunks = this.switchByteOrder()
				? ChunksWrapperByteReversing.New(filledContentBuffer)
				: ChunksWrapper.New(filledContentBuffer)
			;
			
			return X.<Binary>Constant(chunks);
		}

		@Override
		protected void internalWrite(final ComConnection connection, final Binary chunk)
			throws PersistenceExceptionTransfer
		{
			final ByteBuffer defaultBuffer = ComBinary.setChunkHeaderContentLength(
				this.ensureDefaultBufferWrite(),
				chunk.totalLength(),
				this.switchByteOrder()
			);
				
			ComBinary.setChunkHeaderContentLengthChecksum(
					defaultBuffer,
					ComBinary.calculateChunkHeaderContentLengthChecksum(defaultBuffer),
					this.switchByteOrder()
				);
						
			try
			{
				ComBinary.writeChunk(connection, defaultBuffer, chunk.buffers());
			}
			catch(final ComException e)
			{
				throw new PersistenceExceptionTransfer(e);
			}
		}
		
		private final void close()
		{
			this.getConnection().close();
		}
		
		@Override
		public void prepareSource()
		{
			// nothing to prepare when using a SocketChannel
		}
		
		@Override
		public void prepareTarget()
		{
			// nothing to prepare when using a SocketChannel
		}
		
		@Override
		public void closeSource()
		{
			// SocketChannel#close is idempotent
			this.close();
		}
		
		@Override
		public void closeTarget()
		{
			// SocketChannel#close is idempotent
			this.close();
		}
		
		@Override
		public final void validateIsWritable()
		{
			this.writeController.validateIsWritable();
		}
		
		@Override
		public final boolean isWritable()
		{
			return this.writeController.isWritable();
		}
		
		@Override
		public void validateIsStoringEnabled()
		{
			this.writeController.validateIsStoringEnabled();
		}
		
		@Override
		public boolean isStoringEnabled()
		{
			return this.writeController.isStoringEnabled();
		}
		
		@Deprecated
		static void DEBUG_printBufferBinaryValues(final ByteBuffer bb)
		{
			final byte[] bytes = new byte[bb.limit()];
			XMemory.copyRangeToArray(XMemory.getDirectByteBufferAddress(bb), bytes);
			final VarString vs = VarString.New().addHexDec(bytes);
			XDebug.println(vs.toString(), 1);
		}
		
		@Deprecated
		void DEBUG_printTargetByteOrder()
		{
			XDebug.println(
				"TargetByteOrder = " + this.byteOrderTargeting.getTargetByteOrder()
				+ " (requires switching: " + (this.byteOrderTargeting.isByteOrderMismatch() ? "yes" : "no") + ")",
				1
			);
		}
		
	}
	
}
