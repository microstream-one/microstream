package net.jadoth.storage.types;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import net.jadoth.X;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.storage.exceptions.StorageExceptionIoReading;

@FunctionalInterface
public interface StorageFileEntityDataIterator
{
	public <A extends EntityDataAcceptor> A iterateEntityData(
		StorageFile file           ,
		long        fileOffset     ,
		long        iterationLength,
		A           logic
	);
	
	
	public interface Internal extends StorageFileEntityDataIterator
	{
		@Override
		public default <A extends EntityDataAcceptor> A iterateEntityData(
			final StorageFile file           ,
			final long        fileOffset     ,
			final long        iterationLength,
			final A           logic
		)
		{
			this.fillBuffer(file, fileOffset, iterationLength);
			this.iterateFilledBuffer(logic);
			return logic;
		}
		
		public void fillBuffer(
			StorageFile file           ,
			long        fileOffset     ,
			long        iterationLength
		);
		
		public void iterateFilledBuffer(
			EntityDataAcceptor logic
		);
		
		public void validateIterationRange(
			StorageFile file            ,
			long        actualFileLength,
			long        fileOffset      ,
			long        iterationLength
		);
		
	}
	
	
	
	public final class Implementation implements StorageFileEntityDataIterator.Internal
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ByteBuffer directByteBuffer;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final ByteBuffer directByteBuffer)
		{
			super();
			this.directByteBuffer = directByteBuffer;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void fillBuffer(final StorageFile file, final long fileOffset, final long iterationLength)
		{
			final ByteBuffer buffer = this.directByteBuffer;
			
			try
			{
				final FileChannel fileChannel = file.fileChannel();
				this.validateIterationRange(file, fileChannel.size(), fileOffset, iterationLength);

				buffer.clear();
				buffer.limit(X.checkArrayRange(iterationLength));
				fileChannel.position(fileOffset);

				// loop is guaranteed to terminate as it depends on validated buffer size and the file length
				do
				{
					fileChannel.read(buffer);
				}
				while(buffer.hasRemaining());
			}
			catch(final Exception e)
			{
				throw new StorageExceptionIoReading(e);
			}
		}
		
		@Override
		public void validateIterationRange(
			final StorageFile file            ,
			final long        actualFileLength,
			final long        fileOffset      ,
			final long        iterationLength
		)
		{
			if(fileOffset < 0 || fileOffset > actualFileLength)
			{
				// (27.02.2019 TM)EXCP: proper exception
				throw new StorageExceptionIoReading(
					"Invalid file offset " + fileOffset
					+ " specified for " + actualFileLength
					+ " bytes long file \"" + file.identifier() + "\"."
				);
			}
			
			if(iterationLength < 0)
			{
				// (27.02.2019 TM)EXCP: proper exception
				throw new StorageExceptionIoReading(
					"Invalid negative iteration length " + iterationLength
					+ " specified for file \"" + file.identifier() + "\"."
				);
			}
			
			if(fileOffset + iterationLength > actualFileLength)
			{
				// (27.02.2019 TM)EXCP: proper exception
				throw new StorageExceptionIoReading(
					"Invalid iteration range [" + fileOffset + "; " + (fileOffset + iterationLength) + "]"
					+ " specified for " + actualFileLength
					+ " bytes long file \"" + file.identifier() + "\"."
				);
			}
		}
		
		private long calculateIterationBoundAddress(final long bufferBoundAddress)
		{
			/*
			 * Explanation:
			 * bufferBoundAddress points beyond the last byte to be read.
			 * (bufferBoundAddress - headerLength) points at the last (possible) entity header start.
			 * (bBA - hL + 1) points beyond the last (possible) entity header start.
			 * Example:
			 * The whole data to be read ist just the head header of a stateless entity.
			 * So the buffer position equals 24.
			 * Say bufferStartAddress equals 100, meaning bufferBoundAddress equals 124.
			 * Then the iteration bound address (always checked with "<") must be 101:
			 * 124 - 24 + 1.
			 * It must be possible to read an entity as late as address 100, but not beyond that.
			 */
			return bufferBoundAddress - Binary.entityHeaderLength() + 1;
		}

		@Override
		public void iterateFilledBuffer(final EntityDataAcceptor logic)
		{
			final long bufferStartAddress = XMemory.getDirectByteBufferAddress(this.directByteBuffer);
			final long bufferBoundAddress = bufferStartAddress + this.directByteBuffer.position();
			final long iterationBoundAddress = this.calculateIterationBoundAddress(bufferStartAddress);
			
			
			
			
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME StorageFileEntityDataIterator.Internal#iterateFilledBuffer()
		}
		
	}
	
	
	
	public interface EntityDataAcceptor
	{
		public void acceptEntityData(long entityStartAddress);
	}
	
	
	@FunctionalInterface
	public interface Creator
	{
		public StorageFileEntityDataIterator createStorageFileEntityIterator(
			long requiredBufferCapacity
		);
		
	}
	
}
