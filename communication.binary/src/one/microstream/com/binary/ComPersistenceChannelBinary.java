package one.microstream.com.binary;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import one.microstream.X;
import one.microstream.afs.WriteController;
import one.microstream.chars.VarString;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.com.ComException;
import one.microstream.com.ComPersistenceChannel;
import one.microstream.com.XSockets;
import one.microstream.memory.XMemory;
import one.microstream.meta.XDebug;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.ChunksWrapper;
import one.microstream.persistence.binary.types.ChunksWrapperByteReversing;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.ByteOrderTargeting;
import one.microstream.util.BufferSizeProvider;


public interface ComPersistenceChannelBinary<C> extends ComPersistenceChannel<C, Binary>
{
	public static ComPersistenceChannelBinary.Default New(
		final SocketChannel         channel           ,
		final BufferSizeProvider    bufferSizeProvider,
		final ByteOrderTargeting<?> byteOrderTargeting,
		final WriteController       writeController
	)
	{
		return new ComPersistenceChannelBinary.Default(
			notNull(channel)           ,
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
		private       ByteBuffer         defaultBuffer     ;
		
		
		
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
		
		protected ByteBuffer ensureDefaultBuffer()
		{
			if(this.defaultBuffer == null)
			{
				this.defaultBuffer = XMemory.allocateDirectNative(
					this.bufferSizeProvider.provideBufferSize()
				);
			}
			
			return this.defaultBuffer;
		}
		
	}
	

	
	public final class Default extends ComPersistenceChannelBinary.Abstract<SocketChannel>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final ByteOrderTargeting<?> byteOrderTargeting;
		private final WriteController       writeController   ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final SocketChannel         channel           ,
			final BufferSizeProvider    bufferSizeProvider,
			final ByteOrderTargeting<?> byteOrderTargeting,
			final WriteController       writeController
		)
		{
			super(channel, bufferSizeProvider);
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
		protected XGettingCollection<? extends Binary> internalRead(final SocketChannel channel)
			throws PersistenceExceptionTransfer
		{
			final ByteBuffer defaultBuffer = this.ensureDefaultBuffer();
			
//			this.DEBUG_printTargetByteOrder();
			
			ByteBuffer filledContentBuffer;
			try
			{
				filledContentBuffer = ComBinary.readChunk(
					channel,
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
			
//			DEBUG_printBufferBinaryValues(filledContentBuffer);
			
			final ChunksWrapper chunks = this.switchByteOrder()
				? ChunksWrapperByteReversing.New(filledContentBuffer)
				: ChunksWrapper.New(filledContentBuffer)
			;
			
			return X.<Binary>Constant(chunks);
		}

		@Override
		protected void internalWrite(final SocketChannel channel, final Binary chunk)
			throws PersistenceExceptionTransfer
		{
//			this.DEBUG_printTargetByteOrder();
			
			final ByteBuffer defaultBuffer = ComBinary.setChunkHeaderContentLength(
				this.ensureDefaultBuffer(),
				chunk.totalLength(),
				this.switchByteOrder()
			);
			
//			for(final ByteBuffer bb : chunk.buffers())
//			{
//				DEBUG_printBufferBinaryValues(bb);
//			}
			
			try
			{
				this.validateIsWritable();
				
				ComBinary.writeChunk(channel, defaultBuffer, chunk.buffers());
			}
			catch(final ComException e)
			{
				throw new PersistenceExceptionTransfer(e);
			}
		}
		
		private final void close()
		{
			XSockets.closeChannel(this.getConnection());
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
