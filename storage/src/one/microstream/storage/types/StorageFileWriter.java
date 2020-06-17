package one.microstream.storage.types;

import java.io.IOException;
import java.nio.ByteBuffer;

import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.exceptions.StorageExceptionIo;


/**
 * Function type that encapsulates handling of all writing accesses to persistent data, including copying,
 * truncation, deletion.
 *
 * @author TM
 */
public interface StorageFileWriter
{
	public static long validateIoByteCount(final long specifiedByteCount, final long actualByteCount)
	{
		if(specifiedByteCount == actualByteCount)
		{
			return actualByteCount; // validation successful
		}

		// (28.06.2013 TM)EXCP: proper exception
		throw new StorageException(
			"Inconsistent IO operation: actual byte count " + actualByteCount
			+ " does not match the specified byte count if  " + specifiedByteCount + "."
		);
	}
	
	public default long write(final StorageFile file, final Iterable<? extends ByteBuffer> buffers)
	{
		return file.writeBytes(buffers);
	}

//	public default long copy(
//		final ZStorageLockedFile sourceFile,
//		final ZStorageLockedFile targetfile
//	)
//	{
//		return this.copyFilePart(sourceFile, 0, sourceFile.length(), targetfile);
//	}
//
//	public default long copyFilePart(
//		final ZStorageLockedFile sourceFile  ,
//		final long              sourceOffset,
//		final long              length      ,
//		final ZStorageLockedFile targetfile
//	)
//	{
////		DEBUGStorage.println("storage copy file range");
//
//		try
//		{
//			final long byteCount = sourceFile.fileChannel().transferTo(sourceOffset, length, targetfile.fileChannel());
//			targetfile.fileChannel().force(false);
//
//			return validateIoByteCount(length, byteCount);
//		}
//		catch(final IOException e)
//		{
//			throw new StorageException(e); // (01.10.2014 TM)EXCP: proper exception
//		}
//	}
	
	public default long writeStore(
		final StorageLiveDataFile            targetFile ,
		final Iterable<? extends ByteBuffer> byteBuffers
	)
	{
		return this.write(targetFile, byteBuffers);
	}
	
	/**
	 * Logically the same as a store, but technically the same as a transfer with an external source file.
	 */
	public default long writeImport(
		final StorageFile         sourceFile  ,
		final long                sourceOffset,
		final long                copyLength  ,
		final StorageLiveDataFile targetFile
	)
	{
		return sourceFile.copyTo(targetFile, sourceOffset, copyLength);
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
		final StorageLiveDataFile file               ,
		final long                newLength          ,
		final StorageFileProvider storageFileProvider
	)
	{
		truncateFile(file, newLength, storageFileProvider);
	}
	
	public static void truncateFile(
		final StorageLiveDataFile file               ,
		final long                newLength          ,
		final StorageFileProvider storageFileProvider
	)
	{
//		DEBUGStorage.println("storage file truncation");
		final StorageBackupDataFile truncationTargetFile = storageFileProvider.provideTruncationBackupTargetFile(
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
			// (01.10.2014 TM)EXCP: proper exception
			throw new StorageExceptionIo(e);
		}
	}

	public default void delete(
		final StorageLiveDataFile file               ,
		final StorageFileProvider storageFileProvider
	)
	{
		deleteFile(file, storageFileProvider);
	}
	
	public static void deleteFile(
		final StorageLiveDataFile file               ,
		final StorageFileProvider storageFileProvider
	)
	{
//		DEBUGStorage.println("storage file deletion");

		if(rescueFromDeletion(file, storageFileProvider))
		{
			return;
		}
		
		// (16.06.2020 TM)FIXME: priv#49: delete
		
		if(file.delete())
		{
			return;
		}
		
		throw new StorageException("Could not delete file " + file); // (02.10.2014 TM)EXCP: proper exception
	}
	

	public static void createFileFullCopy(
		final StorageFile          sourceFile,
		final StorageCreatableFile targetFile
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
			
			targetFile.ensure();
			sourceFile.copyTo(targetFile);
		}
		catch(final Exception e)
		{
			throw new StorageExceptionIo(e); // (04.03.2015 TM)EXCP: proper exception
		}
	}
	
	public static boolean rescueFromDeletion(
		final StorageLiveDataFile file               ,
		final StorageFileProvider storageFileProvider
	)
	{
		final StorageBackupDataFile deletionTargetFile = storageFileProvider.provideDeletionTargetFile(file);
		if(deletionTargetFile == null)
		{
			return false;
		}
		
		file.moveTo(deletionTargetFile);
		
		return true;
	}
	
	public default void flush(final ZStorageLockedFile targetfile)
	{
		try
		{
			targetfile.fileChannel().force(false);
		}
		catch(final IOException e)
		{
			throw new StorageException(e); // (01.10.2014 TM)EXCP: proper exception
		}
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
