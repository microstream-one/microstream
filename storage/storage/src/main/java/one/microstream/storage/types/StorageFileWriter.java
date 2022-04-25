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

import java.io.IOException;
import java.nio.ByteBuffer;

import one.microstream.afs.types.AFS;
import one.microstream.afs.types.AFile;
import one.microstream.storage.exceptions.StorageExceptionIo;
import one.microstream.storage.exceptions.StorageExceptionIoWriting;


/**
 * Function type that encapsulates handling of all writing accesses to persistent data, including copying,
 * truncation, deletion.
 *
 * 
 */
public interface StorageFileWriter
{
	public static long validateIoByteCount(final long specifiedByteCount, final long actualByteCount)
	{
		if(specifiedByteCount == actualByteCount)
		{
			return actualByteCount; // validation successful
		}

		throw new StorageExceptionIoWriting(
			"Inconsistent IO operation: actual byte count " + actualByteCount
			+ " does not match the specified byte count if  " + specifiedByteCount + "."
		);
	}
	
	public default long write(final StorageFile file, final Iterable<? extends ByteBuffer> buffers)
	{
		return file.writeBytes(buffers);
	}
	
	public default long writeStore(
		final StorageLiveDataFile            targetFile ,
		final Iterable<? extends ByteBuffer> byteBuffers
	)
	{
		return this.write(targetFile, byteBuffers);
	}
	
	/**
	 * Logically the same as a store, but technically the same as a transfer with an external source file.
	 * 
	 * @param source the import source
	 * @param sourceOffset the source offset
	 * @param copyLength the copy length
	 * @param targetFile the target file
	 * @return the amount of bytes written
	 */
	public default long writeImport(
		final StorageImportSource source      ,
		final long                sourceOffset,
		final long                copyLength  ,
		final StorageLiveDataFile targetFile
	)
	{
		return source.copyTo(targetFile, sourceOffset, copyLength);
	}
	
	public default long writeTransfer(
		final StorageLiveDataFile sourceFile  ,
		final long                sourceOffset,
		final long                copyLength  ,
		final StorageLiveDataFile targetFile
	)
	{
		return sourceFile.copyTo(targetFile, sourceOffset, copyLength);
	}
	
	public default long writeTransactionEntryCreate(
		final StorageLiveTransactionsFile    transactionFile,
		final Iterable<? extends ByteBuffer> byteBuffers    ,
		final StorageLiveDataFile            dataFile
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default long writeTransactionEntryStore(
		final StorageLiveTransactionsFile    transactionFile,
		final Iterable<? extends ByteBuffer> byteBuffers    ,
		final StorageLiveDataFile            dataFile       ,
		final long                           dataFileOffset ,
		final long                           storeLength
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default long writeTransactionEntryTransfer(
		final StorageLiveTransactionsFile    transactionFile,
		final Iterable<? extends ByteBuffer> byteBuffers    ,
		final StorageLiveDataFile            dataFile       ,
		final long                           dataFileOffset ,
		final long                           storeLength
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default long writeTransactionEntryDelete(
		final StorageLiveTransactionsFile    transactionFile,
		final Iterable<? extends ByteBuffer> byteBuffers    ,
		final StorageLiveDataFile            dataFile
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default long writeTransactionEntryTruncate(
		final StorageLiveTransactionsFile    transactionFile,
		final Iterable<? extends ByteBuffer> byteBuffers    ,
		final StorageLiveDataFile            dataFile       ,
		final long                           newFileLength
	)
	{
		return this.write(transactionFile, byteBuffers);
	}

	public default void truncate(
		final StorageLiveChannelFile<?> file        ,
		final long                      newLength   ,
		final StorageFileProvider       fileProvider
	)
	{
		truncateFile(file, newLength, fileProvider);
	}
	
	public static void truncateFile(
		final StorageTruncatableChannelFile file        ,
		final long                          newLength   ,
		final StorageFileProvider           fileProvider
	)
	{
//		DEBUGStorage.println("storage file truncation");
		final AFile truncationTargetFile = fileProvider.provideTruncationTargetFile(
			file,
			newLength
		);
		if(truncationTargetFile != null)
		{
			createFileFullCopy(file, truncationTargetFile);
		}

		try
		{
			file.truncate(newLength);
		}
		catch(final Exception e)
		{
			throw new StorageExceptionIoWriting(e);
		}
	}

	public default void delete(
		final StorageLiveDataFile    file           ,
		final StorageWriteController writeController,
		final StorageFileProvider    fileProvider
	)
	{
		deleteFile(file, writeController, fileProvider);
	}
	
	public static void deleteFile(
		final StorageChannelFile     file           ,
		final StorageWriteController writeController,
		final StorageFileProvider    fileProvider
	)
	{
//		DEBUGStorage.println("storage file deletion");
		
		// validate BEFORE moving the file away. Deletion means removing the file.
		writeController.validateIsFileDeletionEnabled();

		if(rescueFromDeletion(file, writeController, fileProvider))
		{
			return;
		}
		
		file.delete();
	}
	

	public static void createFileFullCopy(
		final StorageFile sourceFile,
		final AFile       targetFile
	)
	{
		try
		{
			if(!sourceFile.exists())
			{
				throw new IOException("Copying source file does not exist: " + sourceFile);
			}
			if(targetFile.exists())
			{
				throw new IOException("Copying target already exist: " + targetFile);
			}
			
			AFS.executeWriting(targetFile, wf ->
			{
				// copyTo does ensureExists internally
				sourceFile.copyTo(wf);
			});
		}
		catch(final Exception e)
		{
			throw new StorageExceptionIoWriting(e);
		}
	}
	
	public static boolean rescueFromDeletion(
		final StorageChannelFile     file           ,
		final StorageWriteController writeController,
		final StorageFileProvider    fileProvider
	)
	{
		if(!writeController.isDeletionDirectoryEnabled())
		{
			return false;
		}
		
		final AFile deletionTargetFile = fileProvider.provideDeletionTargetFile(file);
		if(deletionTargetFile == null)
		{
			return false;
		}
		
		if(deletionTargetFile.exists())
		{
			throw new StorageExceptionIo("Moving target already exist: " + deletionTargetFile);
		}
		
		try
		{
			writeController.validateIsDeletionDirectoryEnabled();
			
			AFS.executeWriting(deletionTargetFile, wf ->
			{
				/*
				 * Target file explicitely may NOT be ensured to exist since
				 * moving does not implicitly replace an existing file.
				 */
				file.moveTo(wf);
			});
		}
		catch(final Exception e)
		{
			throw new StorageExceptionIoWriting(e);
		}
		
		return true;
	}
	
	public final class Default implements StorageFileWriter
	{
		// since default methods, interfaces should be directly instantiable :(
	}
	
	

	@FunctionalInterface
	public interface Provider
	{
		public StorageFileWriter provideWriter();
		
		public default StorageFileWriter provideWriter(final int channelIndex)
		{
			return this.provideWriter();
		}

		public final class Default implements StorageFileWriter.Provider
		{
			@Override
			public StorageFileWriter provideWriter()
			{
				return new StorageFileWriter.Default();
			}
		}

	}
	
}
