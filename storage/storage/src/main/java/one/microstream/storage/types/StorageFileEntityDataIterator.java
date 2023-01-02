package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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

import one.microstream.X;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.BinaryEntityRawDataAcceptor;
import one.microstream.persistence.binary.types.BinaryEntityRawDataIterator;
import one.microstream.storage.exceptions.StorageExceptionIoReading;


public interface StorageFileEntityDataIterator
{
	public long iterateEntityData(
		StorageDataFile             file           ,
		long                        fileOffset     ,
		long                        iterationLength,
		BinaryEntityRawDataIterator dataIterator   ,
		BinaryEntityRawDataAcceptor dataAcceptor
	);
	
	public long bufferCapacity();
	
	public StorageFileEntityDataIterator ensureBufferCapacity(long requiredBufferCapacity);
	
	public void removeBuffer();
	
	
	/* (01.03.2019 TM)NOTE:
	 * Experimental concept to differentiate a type internally without inflating the external API.
	 * In order to be able to use wrapping implementations with that, every method of the internal API
	 * must pass the "actual instance" along to switch back to the wrapper instead of staying in the wrapped instance.
	 */
	public interface Internal extends StorageFileEntityDataIterator
	{
		@Override
		public default long iterateEntityData(
			final StorageDataFile             file           ,
			final long                        fileOffset     ,
			final long                        iterationLength,
			final BinaryEntityRawDataIterator dataIterator   ,
			final BinaryEntityRawDataAcceptor dataAcceptor
		)
		{
			this.fillBuffer(this, file, fileOffset, iterationLength);
			
			return this.iterateFilledBuffer(this, dataIterator, dataAcceptor);
		}
		
		public default void prepareFile(
			final StorageFileEntityDataIterator.Internal self           ,
			final StorageDataFile                        file           ,
			final long                                   fileOffset     ,
			final long                                   iterationLength
		)
		{
			// no-op by default
		}
		
		public default void wrapUpFile(
			final StorageFileEntityDataIterator.Internal self           ,
			final StorageDataFile                        file           ,
			final long                                   fileOffset     ,
			final long                                   iterationLength
		)
		{
			// no-op by default
		}
		
		public void fillBuffer(
			StorageFileEntityDataIterator.Internal self           ,
			StorageDataFile                        file           ,
			long                                   fileOffset     ,
			long                                   iterationLength
		);
		
		public long iterateFilledBuffer(
			StorageFileEntityDataIterator.Internal self        ,
			BinaryEntityRawDataIterator            dataIterator,
			BinaryEntityRawDataAcceptor            dataAcceptor
		);
		
		public void validateIterationRange(
			StorageFileEntityDataIterator.Internal self            ,
			StorageDataFile                        file            ,
			long                                   actualFileLength,
			long                                   fileOffset      ,
			long                                   iterationLength
		);
		
	}
	
	
	
	public static StorageFileEntityDataIterator New()
	{
		return new StorageFileEntityDataIterator.Default();
	}
	
	public final class Default implements StorageFileEntityDataIterator.Internal
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private ByteBuffer directByteBuffer;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final long bufferCapacity()
		{
			return this.directByteBuffer != null
				? this.directByteBuffer.capacity()
				: -1L // because 0L is a valid buffer capacity!
			;
		}

		@Override
		public StorageFileEntityDataIterator ensureBufferCapacity(final long requiredBufferCapacity)
		{
			if(this.bufferCapacity() < requiredBufferCapacity)
			{
				XMemory.deallocateDirectByteBuffer(this.directByteBuffer);
				this.directByteBuffer = XMemory.allocateDirectNative(requiredBufferCapacity);
			}
			
			return this;
		}
		
		@Override
		public void removeBuffer()
		{
			this.directByteBuffer = null;
		}

		@Override
		public void fillBuffer(
			final StorageFileEntityDataIterator.Internal self           ,
			final StorageDataFile                        file           ,
			final long                                   fileOffset     ,
			final long                                   iterationLength
		)
		{
			try
			{
				this.prepareFile(self, file, fileOffset, iterationLength);
				
				self.validateIterationRange(self, file, file.size(), fileOffset, iterationLength);
				self.ensureBufferCapacity(iterationLength);
				final ByteBuffer buffer = this.directByteBuffer;
				buffer.clear();
				buffer.limit(X.checkArrayRange(iterationLength));
				
				file.readBytes(buffer, fileOffset);
			}
			catch(final Exception e)
			{
				throw new StorageExceptionIoReading(e);
			}
			finally
			{
				self.wrapUpFile(self, file, fileOffset, iterationLength);
			}
		}
		
		@Override
		public void validateIterationRange(
			final StorageFileEntityDataIterator.Internal self            ,
			final StorageDataFile                        file            ,
			final long                                   actualFileLength,
			final long                                   fileOffset      ,
			final long                                   iterationLength
		)
		{
			if(fileOffset < 0 || fileOffset > actualFileLength)
			{
				throw new StorageExceptionIoReading(
					"Invalid file offset " + fileOffset
					+ " specified for " + actualFileLength
					+ " bytes long file \"" + file.identifier() + "\"."
				);
			}
			
			if(iterationLength < 0)
			{
				throw new StorageExceptionIoReading(
					"Invalid negative iteration length " + iterationLength
					+ " specified for file \"" + file.identifier() + "\"."
				);
			}
			
			if(fileOffset + iterationLength > actualFileLength)
			{
				throw new StorageExceptionIoReading(
					"Invalid iteration range [" + fileOffset + "; " + (fileOffset + iterationLength) + "]"
					+ " specified for " + actualFileLength
					+ " bytes long file \"" + file.identifier() + "\"."
				);
			}
		}
		
		@Override
		public long iterateFilledBuffer(
			final StorageFileEntityDataIterator.Internal self        ,
			final BinaryEntityRawDataIterator            dataIterator,
			final BinaryEntityRawDataAcceptor            dataAcceptor
		)
		{
			final long bufferStartAddress = XMemory.getDirectByteBufferAddress(this.directByteBuffer);
			final long bufferBoundAddress = bufferStartAddress + this.directByteBuffer.position();
			
			return dataIterator.iterateEntityRawData(bufferStartAddress, bufferBoundAddress, dataAcceptor);
		}
		
	}
		
}
