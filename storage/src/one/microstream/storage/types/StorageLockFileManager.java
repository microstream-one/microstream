package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.XArrays;
import one.microstream.concurrency.XThreads;
import one.microstream.memory.PlatformInternals;
import one.microstream.memory.XMemory;
import one.microstream.meta.XDebug;

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
	
	
	
	public static StorageLockFileManager New(
		final StorageLockFileSetup       setup              ,
		final StorageOperationController operationController,
		final StorageFileReader          reader             ,
		final StorageFileWriter          writer
	)
	{
		return new StorageLockFileManager.Default(
			notNull(setup),
			notNull(operationController),
			notNull(reader),
			notNull(writer)
		);
	}
	
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
		private transient boolean           isRunning        ;
		private transient StorageLockedFile lockFile         ;
		private transient LockFileData      lockFileData     ;
		private transient ByteBuffer[]      wrappedByteBuffer;
		private transient ByteBuffer        directByteBuffer ;
		private transient byte[]            stringReadBuffer ;
		private transient byte[]            stringWriteBuffer;
		private transient VarString         vs;
		
		
		
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
			this.vs                  = VarString.New()    ;
			
			// 2 timestamps with separators and an identifier. Should suffice.
			this.wrappedByteBuffer = new ByteBuffer[1];

			this.stringReadBuffer = new byte[64];
			this.stringWriteBuffer = this.stringReadBuffer.clone();
			this.allocateBuffer(this.stringReadBuffer.length);
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
		public StorageLockFileManager.Default start()
		{
			this.ensureInitialized();
			StorageLockFileManager.super.start();
			return this;
		}

		@Override
		public final void run()
		{
			final long updateInterval = this.setup.updateInterval();
			
			try
			{
				this.checkInitialized();
				
				XDebug.println("Lock File Manager is running ...");
				
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
			
			try
			{
				this.initialize();
			}
			catch(final Exception e)
			{
				this.operationController.registerDisruptingProblem(e);
				this.ensureClosedFile();
				throw e;
			}
		}
		
		private void checkInitialized()
		{
			if(this.lockFile != null)
			{
				return;
			}
			
			// (12.04.2019 TM)EXCP: proper exception
			throw new RuntimeException(StorageLockFileManager.class.getSimpleName() + " not initialized.");
		}
		
		private ByteBuffer ensureReadingBuffer(final int fileLength)
		{
			ensureBufferCapacity(fileLength);
			if(this.stringReadBuffer.length != fileLength)
			{
				this.stringReadBuffer = new byte[fileLength];
			}
			
			this.directByteBuffer.limit(fileLength);
			
			return this.directByteBuffer;
		}
		
		private ByteBuffer[] ensureWritingBuffer(final byte[] bytes)
		{
			ensureBufferCapacity(bytes.length);
			this.directByteBuffer.limit(bytes.length);
			
			this.stringWriteBuffer = bytes;
			
			return this.wrappedByteBuffer;
		}
		
		private boolean ensureBufferCapacity(final int capacity)
		{
			if(this.directByteBuffer.capacity() >= capacity)
			{
				// already enough capacity
				return false;
			}
			
			/* data has to be copied 3242 times in JDK to load a single String.
			 * Note that using a byte[]-wrapping ByteBuffer is not better since the geniuses
			 * use a TemporaryDirectBuffer internally for non-direct buffers that adds the copying step anyway.
			 * The only reasonable thing to use with nio is the DirectByteBuffer, despite all the missing API
			 * and API hiding issues.
			 */
			PlatformInternals.deallocateDirectBuffer(this.directByteBuffer);
			this.allocateBuffer(capacity);
			
			return true;
		}
		
		private void allocateBuffer(final int capacity)
		{
			this.wrappedByteBuffer[0] = this.directByteBuffer = ByteBuffer.allocateDirect(capacity);
		}
		
		private String readString()
		{
			this.fillReadBufferFromFile();
			
			return new String(this.stringReadBuffer, this.setup.charset());
		}
		
		private void fillReadBufferFromFile()
		{
			final int fileLength = X.checkArrayRange(this.lockFile.length());
			this.reader.readStorage(this.lockFile, 0, this.ensureReadingBuffer(fileLength), this);
			XMemory.copyRangeToArray(PlatformInternals.getDirectBufferAddress(this.directByteBuffer), this.stringReadBuffer);
		}
						
		private LockFileData readLockFileData()
		{
			final String currentFileData = this.readString();
			
			// since JDK 9's moronic String change, there's even one more copying required. Endless copying ...
			final char[] chars = currentFileData.toCharArray();
			
			final int sep1Index = indexOfFirstNonNumberCharacter(chars, 0);
			final int sep2Index = indexOfFirstNonNumberCharacter(chars, sep1Index + 1);
			
			final long   currentTime    = XChars.parse_longDecimal(chars, 0, sep1Index);
			final long   expirationTime = XChars.parse_longDecimal(chars, sep1Index + 1, sep2Index - sep1Index - 1);
			final String identifier     = String.valueOf(chars, sep2Index + 1, chars.length - sep2Index - 1);
			
			return new LockFileData(currentTime, expirationTime, identifier);
		}
		
		static final int indexOfFirstNonNumberCharacter(final char[] data, final int offset)
		{
			for(int i = offset; i < data.length; i++)
			{
				if(data[i] < '0' || data[i] > '9')
				{
					return i;
				}
			}
			
			// (10.04.2019 TM)EXCP: proper exception
			throw new RuntimeException("No separator found in lock file string.");
		}
		
		static final class LockFileData
		{
			      long   lastWriteTime ;
			      long   expirationTime;
			final String identifier    ;
			final long   updateInterval;
			
			LockFileData(final String identifier, final long updateInterval)
			{
				super();
				this.identifier     = identifier    ;
				this.updateInterval = updateInterval;
			}
			
			LockFileData(final long lastWriteTime, final long expirationTime, final String identifier)
			{
				this(identifier, deriveUpdateInterval(lastWriteTime, expirationTime));
				this.lastWriteTime  = lastWriteTime ;
				this.expirationTime = expirationTime;
			}
			
			final void update()
			{
				this.lastWriteTime  = System.currentTimeMillis();
				this.expirationTime = this.lastWriteTime + this.updateInterval;
			}
			
			private static long deriveUpdateInterval(final long lastWriteTime, final long expirationTime)
			{
				final long derivedInterval = expirationTime - lastWriteTime;
				if(derivedInterval <= 0)
				{
					// (10.04.2019 TM)EXCP: proper exception
					throw new RuntimeException(
						"Invalid lockfile timestamps: lastWriteTime = " + lastWriteTime
						+ ", expirationTime = " + expirationTime
					);
				}
				
				return derivedInterval;
			}
			
			/**
			 * "long" meaning the expiration time has been passed by another interval.
			 * This is a tolerance / grace time strategy to exclude
			 * @return
			 */
			final boolean isLongExpired()
			{
				return System.currentTimeMillis() > this.expirationTime + this.updateInterval;
			}
			
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
			this.lockFile = fileProvider.provideLockFile();
			
			if(this.lockFile.exists() && this.lockFile.length() > 0)
			{
				this.validateExistingLockFileData(true);
			}

			this.lockFileData = new LockFileData(this.setup.processIdentity(), this.setup.updateInterval());
			
//			XDebug.println(
//				"Initial lock data: " + this.lockFileData.identifier + " update=" + this.lockFileData.updateInterval
//			);
			
			this.writeLockFileData();
		}
		
		private void validateExistingLockFileData(final boolean firstAttempt)
		{
			final LockFileData existingFiledata = this.readLockFileData();
			
			final String identifier = this.setup.processIdentity();
			if(identifier.equals(existingFiledata.identifier))
			{
				// database is already owned by "this" process (e.g. crash shorty before), so just continue and reuse.
				return;
			}
			
			if(existingFiledata.isLongExpired())
			{
				/*
				 * The lock file is no longer updated, meaning the database is not used anymore
				 * and the lockfile is just a zombie, probably left by a crash.
				 */
				return;
			}
			
			// not owned and not expired
			if(firstAttempt)
			{
				// wait one interval and try a second time
				XThreads.sleep(existingFiledata.updateInterval);
				validateExistingLockFileData(false);
				
				// reaching here means no exception (but expiration) on the second attempt, meaning success.
				return;
			}
			
			// not owned, not expired and still active, meaning really still in use, so exception

			// (10.04.2019 TM)EXCP: proper exception
			throw new RuntimeException("Storage already in use by: " + existingFiledata.identifier);
		}
		
		private void checkForModifiedLockFile()
		{
			this.fillReadBufferFromFile();
			
			// performance-optimized JDK method
			if(XArrays.equals(this.stringReadBuffer, this.stringWriteBuffer, this.stringWriteBuffer.length))
			{
//				XDebug.println(
//					"Current LockFile is valid: " + new String(this.stringReadBuffer, this.setup.charset())
//				);
				return;
			}

			// (11.04.2019 TM)EXCP: proper exception
			throw new RuntimeException("Concurrent lock file modification detected.");
		}
		
		private void writeLockFileData()
		{
			this.lockFileData.update();
			
//			XDebug.println(
//				"Writing lock data: "
//				+ XDebug.formatCommonTime(new Date(this.lockFileData.lastWriteTime)) + ";"
//				+ XDebug.formatCommonTime(new Date(this.lockFileData.expirationTime)) + ";"
//				+ this.lockFileData.identifier
//			);
			
			this.vs.reset()
			.add(this.lockFileData.lastWriteTime).add(';')
			.add(this.lockFileData.expirationTime).add(';')
			.add(this.lockFileData.identifier)
			;
			
			final byte[] bytes = this.vs.encodeBy(this.setup.charset());
			final ByteBuffer[] bb = this.ensureWritingBuffer(bytes);
			
			XMemory.copyArrayToAddress(bytes, PlatformInternals.getDirectBufferAddress(this.directByteBuffer));
			
			this.writer.write(this.lockFile, bb);
		}
				
		private void updateFile()
		{
			// check again after the wait time.
			if(!this.checkIsRunning())
			{
				// abort to avoid an unnecessary write.
				return;
			}
			
			this.checkForModifiedLockFile();
			this.writeLockFileData();
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
	
	
	public static Creator Creator()
	{
		return new Creator.Default();
	}
	
	public interface Creator
	{
		public StorageLockFileManager createLockFileManager(
			StorageLockFileSetup       setup              ,
			StorageOperationController operationController,
			StorageFileReader          reader             ,
			StorageFileWriter          writer
		);
		
		public final class Default implements StorageLockFileManager.Creator
		{
			Default()
			{
				super();
			}

			@Override
			public StorageLockFileManager createLockFileManager(
				final StorageLockFileSetup       setup              ,
				final StorageOperationController operationController,
				final StorageFileReader          reader             ,
				final StorageFileWriter          writer
			)
			{
				return StorageLockFileManager.New(
					setup              ,
					operationController,
					reader             ,
					writer
				);
			}
			
		}
		
	}
	
}
