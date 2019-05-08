package one.microstream.storage.types;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
		throw new RuntimeException(
			"Inconsistent IO operation: actual byte count " + actualByteCount
			+ " does not match the specified byte count if  " + specifiedByteCount + "."
		);
	}
	
	public default long write(final StorageLockedFile file, final ByteBuffer[] byteBuffers)
	{
//		DEBUGStorage.println("storage write multiple buffers");

		// determine last non-empty buffer to be used as a write-completion check point
		final ByteBuffer lastNonEmpty = determineLastNonEmpty(byteBuffers);
		if(lastNonEmpty == null)
		{
			return 0L;
		}
		
		final FileChannel channel   = file.fileChannel();
		final long        oldLength = file.length();
		
		long writeCount = 0;
		try
		{
			channel.position(oldLength);
			while(lastNonEmpty.hasRemaining())
			{
				writeCount += channel.write(byteBuffers);
			}
			
			// this is the right place for a data-safety-securing force/flush.
			channel.force(false);
			
			final long newTotalLength = file.length();
			if(newTotalLength != oldLength + writeCount)
			{
				 // (01.10.2014)EXCP: proper exception
				throw new RuntimeException(
					"Inconsistent post-write file length:"
					+ " New total length " + newTotalLength +
					" is not equal " + oldLength + " + " + writeCount + " (old length and write count)"
				);
			}
			
			return writeCount;
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (01.10.2014)EXCP: proper exception
		}
	}

	public static ByteBuffer determineLastNonEmpty(final ByteBuffer[] byteBuffers)
	{
		for(int i = byteBuffers.length - 1; i >= 0; i--)
		{
			if(byteBuffers[i].hasRemaining())
			{
				return byteBuffers[i];
			}
		}
		
		// either the array is empty or only contains empty buffers. Either way, no suitable buffer found.
		return null;
	}

	public default long copy(
		final StorageLockedFile sourceFile,
		final StorageLockedFile targetfile
	)
	{
		return this.copyFilePart(sourceFile, 0, sourceFile.length(), targetfile);
	}

	public default long copyFilePart(
		final StorageLockedFile sourceFile  ,
		final long              sourceOffset,
		final long              length      ,
		final StorageLockedFile targetfile
	)
	{
//		DEBUGStorage.println("storage copy file range");

		try
		{
			final long byteCount = sourceFile.fileChannel().transferTo(sourceOffset, length, targetfile.fileChannel());
			targetfile.fileChannel().force(false);
			
			return validateIoByteCount(length, byteCount);
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (01.10.2014)EXCP: proper exception
		}
	}
	
	public default long writeStore(
		final StorageDataFile<?> targetFile ,
		final ByteBuffer[]       byteBuffers
	)
	{
		return this.write(targetFile, byteBuffers);
	}
	
	/**
	 * Logically the same as a store, but technically the same as a transfer with an external source file.
	 * 
	 * @param sourceFile
	 * @param sourceOffset
	 * @param copyLength
	 * @param targetfile
	 * @return
	 */
	public default long writeImport(
		final StorageLockedFile  sourceFile  ,
		final long               sourceOffset,
		final long               copyLength  ,
		final StorageDataFile<?> targetFile
	)
	{
		return this.copyFilePart(sourceFile, sourceOffset, copyLength, targetFile);
	}
	
	public default long writeTransfer(
		final StorageDataFile<?> sourceFile  ,
		final long               sourceOffset,
		final long               copyLength  ,
		final StorageDataFile<?> targetFile
	)
	{
		return this.copyFilePart(sourceFile, sourceOffset, copyLength, targetFile);
	}
	
	public default long writeTransactionEntryCreate(
		final StorageInventoryFile transactionFile,
		final ByteBuffer[]         byteBuffers    ,
		final StorageDataFile<?>   dataFile
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default long writeTransactionEntryStore(
		final StorageInventoryFile transactionFile,
		final ByteBuffer[]         byteBuffers    ,
		final StorageDataFile<?>   dataFile       ,
		final long                 dataFileOffset ,
		final long                 storeLength
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default long writeTransactionEntryTransfer(
		final StorageInventoryFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile       ,
		final long                     dataFileOffset ,
		final long                     storeLength
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default long writeTransactionEntryDelete(
		final StorageInventoryFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default long writeTransactionEntryTruncate(
		final StorageInventoryFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageInventoryFile     file           ,
		final long                     newFileLength
	)
	{
		return this.write(transactionFile, byteBuffers);
	}

	public default void truncate(
		final StorageInventoryFile file               ,
		final long                 newLength          ,
		final StorageFileProvider  storageFileProvider
	)
	{
		truncateFile(file, newLength, storageFileProvider);
	}
	
	public static void truncateFile(
		final StorageNumberedFile file               ,
		final long                newLength          ,
		final StorageFileProvider storageFileProvider
	)
	{
//		DEBUGStorage.println("storage file truncation");
		final StorageNumberedFile truncationTargetFile = storageFileProvider.provideTruncationBackupTargetFile(
			file,
			newLength
		);
		if(truncationTargetFile != null)
		{
			craeteFileFullCopy(file, truncationTargetFile);
		}

		try
		{
			file.fileChannel().truncate(newLength);
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (01.10.2014)EXCP: proper exception
		}
	}

	public default void delete(
		final StorageInventoryFile file               ,
		final StorageFileProvider  storageFileProvider
	)
	{
		deleteFile(file, storageFileProvider);
	}
	
	public static void deleteFile(
		final StorageNumberedFile file               ,
		final StorageFileProvider storageFileProvider
	)
	{
//		DEBUGStorage.println("storage file deletion");

		if(rescueFromDeletion(file, storageFileProvider))
		{
			return;
		}
		
		if(file.delete())
		{
			return;
		}
		
		throw new RuntimeException("Could not delete file " + file); // (02.10.2014 TM)EXCP: proper exception
	}
	

	public static void craeteFileFullCopy(
		final StorageNumberedFile sourceFile,
		final StorageNumberedFile targetFile
	)
	{
		try
		{
			final Path source = Paths.get(sourceFile.identifier());
			final Path target = Paths.get(targetFile.identifier());
			if(!Files.exists(source))
			{
				throw new IOException("Copying source file does not exist: " + sourceFile.identifier());
			}
			if(Files.exists(target))
			{
				throw new IOException("Copying target already exist: " + targetFile.identifier());
			}
			
			Files.copy(source, target);
		}
		catch(final Exception e)
		{
			throw new StorageExceptionIo(e); // (04.03.2015 TM)EXCP: proper exception
		}
	}
	
	public static boolean rescueFromDeletion(
		final StorageNumberedFile file               ,
		final StorageFileProvider storageFileProvider
	)
	{
		final StorageNumberedFile deletionTargetFile = storageFileProvider.provideDeletionTargetFile(file);
		if(deletionTargetFile == null)
		{
			return false;
		}
		
		try
		{
			final Path source = Paths.get(file.identifier());
			final Path target = Paths.get(deletionTargetFile.identifier());
			Files.move(source, target);
		}
		catch(final Exception e)
		{
			throw new StorageExceptionIo(e); // (04.03.2015 TM)EXCP: proper exception
		}
		
		return true;
	}
	
	public default void flush(final StorageLockedFile targetfile)
	{
		try
		{
			targetfile.fileChannel().force(false);
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (01.10.2014)EXCP: proper exception
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
