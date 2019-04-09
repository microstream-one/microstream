package one.microstream.storage.types;

import java.io.IOException;
import java.nio.ByteBuffer;

import one.microstream.X;
import one.microstream.concurrency.XThreads;
import one.microstream.memory.XMemory;

public interface StorageLockFileManager extends Runnable
{
	public default StorageLockFileManager start()
	{
		this.setRunning(true);
		return this;
	}
	
	public default StorageLockFileManager stop()
	{
		this.setRunning(false);
		return this;
	}
	
	public boolean isRunning();
	
	public StorageLockFileManager setRunning(boolean running);
	
	
	
	public final class Default implements StorageLockFileManager, StorageReaderCallback
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageLockFileSetup       setup              ;
		private final StorageOperationController operationController;
		private final StorageFileReader          reader             ;
		private final StorageFileWriter          writer             ;

		// cached values
		private transient boolean           isRunning;
		private transient StorageLockedFile lockFile;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final StorageLockFileSetup       setup              ,
			final StorageOperationController operationController,
			final StorageFileReader          reader             ,
			final StorageFileWriter          writer
		)
		{
			super();
			this.setup               = setup              ;
			this.operationController = operationController;
			this.reader              = reader             ;
			this.writer              = writer             ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public synchronized final boolean isRunning()
		{
			return this.isRunning;
		}

		@Override
		public synchronized final StorageLockFileManager setRunning(final boolean running)
		{
			this.isRunning = running;
			
			return this;
		}
		
		private synchronized boolean checkIsRunning()
		{
			return this.isRunning && this.operationController.checkProcessingEnabled();
		}

		@Override
		public final void run()
		{
			final long updateInterval = this.setup.updateInterval();
			
			try
			{
				// causes the initial write
				this.ensureInitialized();
				
				// wait first after the intial write, then perform the regular update
				while(this.checkIsRunning())
				{
					XThreads.sleep(updateInterval);
					this.updateFile();
				}
			}
			catch(final Exception e)
			{
				this.operationController.registerDisruptingProblem(e);
				throw e;
			}
			finally
			{
				// ensure closed file in any case. Regular shutdown or forceful shutdown by exception.
				this.ensureClosedFile();
			}
		}
		
		private void ensureInitialized()
		{
			if(this.lockFile != null)
			{
				return;
			}
			this.initialize();
		}

		private ByteBuffer[] singleDirectByteBuffer;
		private ByteBuffer   directByteBuffer      ;
		private byte[]       stringReadBuffer      ;
		private byte[]       stringWriteBuffer     ;
		
		private ByteBuffer ensureReadingBuffer(final int capacity)
		{
			ensureBufferCapacity(capacity);
			return this.directByteBuffer;
		}
		
		private ByteBuffer[] ensureWritingBuffer(final int capacity)
		{
			ensureBufferCapacity(capacity);
			return this.singleDirectByteBuffer;
		}
		
		private boolean ensureBufferCapacity(final int capacity)
		{
			if(this.directByteBuffer.capacity() >= capacity)
			{
				// already enough capacity
				return false;
			}
			
			// data has to be copied 3242 times in JDK to build a single String.
			XMemory.deallocateDirectByteBuffer(this.directByteBuffer);
			this.singleDirectByteBuffer[0] = this.directByteBuffer = ByteBuffer.allocate(capacity);
		}
		
		private String readString()
		{
			final int fileLength = X.checkArrayRange(this.lockFile.length());
			this.reader.readStorage(this.lockFile, 0, this.ensureReadingBuffer(fileLength), this);
			XMemory.copyRangeToArray(XMemory.getDirectByteBufferAddress(this.directByteBuffer), this.stringIoBuffer);
			
			return new String(this.stringIoBuffer, this.setup.charset());
		}
		
		private void readLockFileData()
		{
//			final String
		}
		
		static final class LockFileData
		{
			long   lastWriteTime ;
			long   expirationTime;
			String identifier    ;
		}
		
		@Override
		public void validateIncrementalRead(
			final StorageLockedFile file         ,
			final long              filePosition ,
			final ByteBuffer        buffer       ,
			final long              lastReadCount
		)
			throws IOException
		{
			StorageReaderCallback.staticValidateIncrementalRead(file, filePosition, buffer, lastReadCount);
		}
		
		private void initialize()
		{
			final StorageFileProvider fileProvider = this.setup.lockFileProvider();
			final StorageLockedFile   lockFile     = fileProvider.provideLockFile();
			
			final int fileLength = X.checkArrayRange(lockFile.length());
			this.reader.readStorage(lockFile, 0, this.ensureReadingBuffer(fileLength), this);
			
			// FIXME StorageLockFileManager.Default#initialize()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		private void updateFile()
		{
			// check again after the wait time.
			if(this.checkIsRunning())
			{
				// abort to avoid un unnecessary write.
				return;
			}
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME StorageLockFileManager.Default#updateFile()
		}
		
		private void ensureClosedFile()
		{
			if(this.lockFile == null)
			{
				return;
			}
			
			StorageLockedFile.closeSilent(this.lockFile);
			this.lockFile = null;
		}
		
	}
	
}
