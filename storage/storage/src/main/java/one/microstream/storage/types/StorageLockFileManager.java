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

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

import one.microstream.X;
import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AFileSystem;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.ArrayView;
import one.microstream.collections.XArrays;
import one.microstream.concurrency.XThreads;
import one.microstream.memory.XMemory;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.exceptions.StorageExceptionInitialization;

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
		final StorageOperationController operationController
	)
	{
		return new StorageLockFileManager.Default(
			notNull(setup),
			notNull(operationController)
		);
	}
	
	public final class Default implements StorageLockFileManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageLockFileSetup       setup              ;
		private final StorageOperationController operationController;

		// cached values
		private transient boolean               isRunning               ;
		private transient StorageLockFile       lockFile                ;
		private transient LockFileData          lockFileData            ;
		private transient ByteBuffer[]          wrappedByteBuffer       ;
		private transient ArrayView<ByteBuffer> wrappedWrappedByteBuffer;
		private transient ByteBuffer            directByteBuffer        ;
		private transient byte[]                stringReadBuffer        ;
		private transient byte[]                stringWriteBuffer       ;
		private transient VarString             vs                      ;
		private transient AFileSystem           fileSystem              ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final StorageLockFileSetup       setup              ,
			final StorageOperationController operationController
		)
		{
			super();
			this.setup               = setup              ;
			this.fileSystem          = setup.lockFileProvider().fileSystem();
			this.operationController = operationController;
			this.vs                  = VarString.New()    ;
			
			// 2 timestamps with separators and an identifier. Should suffice.
			this.wrappedByteBuffer = new ByteBuffer[1];
			this.wrappedWrappedByteBuffer = X.ArrayView(this.wrappedByteBuffer);

			this.stringReadBuffer = new byte[64];
			this.stringWriteBuffer = this.stringReadBuffer.clone();
			this.allocateBuffer(this.stringReadBuffer.length);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final synchronized boolean isRunning()
		{
			return this.isRunning;
		}

		@Override
		public final synchronized StorageLockFileManager setRunning(final boolean running)
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
			
			Throwable closingCause = null;
			try
			{
				this.checkInitialized();
				
				// wait first after the initial write, then perform the regular update
				while(this.checkIsRunning())
				{
					XThreads.sleep(updateInterval);
					this.updateFile();
				}
			}
			catch(final Exception e)
			{
				closingCause = e;
				this.operationController.registerDisruption(e);
				throw e;
			}
			finally
			{
				// ensure closed file in any case. Regular shutdown or forceful shutdown by exception.
				this.ensureClosedLockFile(closingCause);
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
				this.operationController.registerDisruption(e);
				this.ensureClosedLockFile(e);
				throw e;
			}
		}
		
		private void checkInitialized()
		{
			if(this.lockFile != null)
			{
				return;
			}
			
			throw new StorageExceptionInitialization(StorageLockFileManager.class.getSimpleName() + " not initialized.");
		}
		
		private ByteBuffer ensureReadingBuffer(final int fileLength)
		{
			this.ensureBufferCapacity(fileLength);
			if(this.stringReadBuffer.length != fileLength)
			{
				this.stringReadBuffer = new byte[fileLength];
			}
			
			this.directByteBuffer.limit(fileLength);
			
			return this.directByteBuffer;
		}
		
		private ArrayView<ByteBuffer> ensureWritingBuffer(final byte[] bytes)
		{
			this.ensureBufferCapacity(bytes.length);
			this.directByteBuffer.limit(bytes.length);
			
			this.stringWriteBuffer = bytes;
			
			return this.wrappedWrappedByteBuffer;
		}
		
		private boolean ensureBufferCapacity(final int capacity)
		{
			if(this.directByteBuffer.capacity() >= capacity)
			{
				// already enough capacity
				return false;
			}
			
			/* data has to be copied multiple times in JDK to load a single String.
			 * Note that using a byte[]-wrapping ByteBuffer is not better since
			 * internally a TemporaryDirectBuffer is used for non-direct buffers that adds the copying step anyway.
			 * The only reasonable thing to use with nio is the DirectByteBuffer.
			 */
			XMemory.deallocateDirectByteBuffer(this.directByteBuffer);
			this.allocateBuffer(capacity);
			
			return true;
		}
		
		private void allocateBuffer(final int capacity)
		{
			this.wrappedByteBuffer[0] = this.directByteBuffer = XMemory.allocateDirectNative(capacity);
		}
		
		private String readString()
		{
			this.fillReadBufferFromFile();
			
			return new String(this.stringReadBuffer, this.setup.charset());
		}
		
		private void fillReadBufferFromFile()
		{
			final int fileLength = X.checkArrayRange(this.lockFile.size());
			this.lockFile.readBytes(this.ensureReadingBuffer(fileLength), 0, fileLength);
			XMemory.copyRangeToArray(XMemory.getDirectByteBufferAddress(this.directByteBuffer), this.stringReadBuffer);
		}
						
		private LockFileData readLockFileData()
		{
			final String currentFileData = this.readString();
			
			// since JDK 9's String change, there's even one more copying required.
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
			
			throw new StorageException("No separator found in lock file string.");
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
					throw new StorageException(
						"Invalid lockfile timestamps: lastWriteTime = " + lastWriteTime
						+ ", expirationTime = " + expirationTime
					);
				}
				
				return derivedInterval;
			}
			
			/**
			 * "long" meaning the expiration time has been passed by another interval.
			 * This is a tolerance / grace time strategy to exclude
			 */
			final boolean isLongExpired()
			{
				return System.currentTimeMillis() > this.expirationTime + this.updateInterval;
			}
			
		}
		
		private boolean lockFileHasContent()
		{
			return this.lockFile.exists() && this.lockFile.size() > 0;
		}
				
		private void initialize()
		{
			final StorageLiveFileProvider fileProvider = this.setup.lockFileProvider();
			final AFile                   lockFile     = fileProvider.provideLockFile();
			this.lockFile = StorageLockFile.New(lockFile);
			
			final LockFileData initialFileData = this.lockFileHasContent()
				? this.validateExistingLockFileData(true)
				: null
			;
			
			if(this.isReadOnlyMode())
			{
				if(initialFileData != null)
				{
					// write buffer must be filled with the file's current content so the check will be successful.
					this.setToWriteBuffer(initialFileData);
				}
				
				// abort, since neither lockFileData nor writing is required/allowed in read-only mode.
				return;
			}

			this.lockFileData = new LockFileData(this.setup.processIdentity(), this.setup.updateInterval());
			
			this.writeLockFileData();
		}
		
		private LockFileData validateExistingLockFileData(final boolean firstAttempt)
		{
			final LockFileData existingFiledata = this.readLockFileData();
			
			final String identifier = this.setup.processIdentity();
			if(identifier.equals(existingFiledata.identifier))
			{
				// database is already owned by "this" process (e.g. crash shorty before), so just continue and reuse.
				return existingFiledata;
			}
			
			if(existingFiledata.isLongExpired())
			{
				/*
				 * The lock file is no longer updated, meaning the database is not used anymore
				 * and the lockfile is just a zombie, probably left by a crash.
				 */
				return existingFiledata;
			}
			
			// not owned and not expired
			if(firstAttempt)
			{
				// wait one interval and try a second time
				XThreads.sleep(existingFiledata.updateInterval);
				return this.validateExistingLockFileData(false);
				
				// returning here means no exception (but expiration) on the second attempt, meaning success.
			}
			
			// not owned, not expired and still active, meaning really still in use, so exception

			throw new StorageException("Storage already in use by: " + existingFiledata.identifier);
		}
		
		private void checkForModifiedLockFile()
		{
			if(this.isReadOnlyMode() && !this.lockFileHasContent())
			{
				// no existing lock file can be ignored in read-only mode.
				return;
			}
			
			this.fillReadBufferFromFile();
			
			// performance-optimized JDK method
			if(XArrays.equals(this.stringReadBuffer, this.stringWriteBuffer, this.stringWriteBuffer.length))
			{
				return;
			}

			throw new StorageException("Concurrent lock file modification detected.");
		}
		
		private boolean isReadOnlyMode()
		{
			return !this.fileSystem.isWritable();
		}
		
		private void writeLockFileData()
		{
			if(this.isReadOnlyMode())
			{
				// do not write in read-only mode. But everything else (modification checking etc.) is still required.
				return;
			}
			
			this.lockFileData.update();
			

			final ArrayView<ByteBuffer> bb = this.setToWriteBuffer(this.lockFileData);
			
			// no need for the writer detour (for now) since it makes no sense to backup lock files.
			this.lockFile.writeBytes(bb);
		}
		
		private ArrayView<ByteBuffer> setToWriteBuffer(final LockFileData lockFileData)
		{
			this.vs.reset()
			.add(lockFileData.lastWriteTime).add(';')
			.add(lockFileData.expirationTime).add(';')
			.add(lockFileData.identifier)
			;
			
			final byte[] bytes = this.vs.encodeBy(this.setup.charset());
			final ArrayView<ByteBuffer> bb = this.ensureWritingBuffer(bytes);
			
			XMemory.copyArrayToAddress(bytes, XMemory.getDirectByteBufferAddress(this.directByteBuffer));
			
			return bb;
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
		
		private void ensureClosedLockFile(final Throwable cause)
		{
			if(this.lockFile == null)
			{
				return;
			}
			
			StorageClosableFile.close(this.lockFile, cause);
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
			StorageOperationController operationController
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
				final StorageOperationController operationController
			)
			{
				return StorageLockFileManager.New(
					setup              ,
					operationController
				);
			}
			
		}
		
	}
	
}
