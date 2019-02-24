package net.jadoth.storage.types;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.jadoth.storage.exceptions.StorageExceptionIo;


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
	
	// (13.02.2019 TM)NOTE: single ByteBuffer variant removed to keep implementations simple.
	public default long write(final StorageLockedFile file, final ByteBuffer[] byteBuffers)
	{
//		DEBUGStorage.println("storage write multiple buffers");

		// determine last non-empty buffer to be used as a write-completion check point
		final ByteBuffer lastNonEmpty = determineLastNonEmpty(byteBuffers);
		if(lastNonEmpty == null)
		{
			return 0L;
		}
		
		final FileChannel channel   = file.channel();
		final long        oldLength = file.length();
		try
		{
			while(lastNonEmpty.hasRemaining())
			{
				channel.write(byteBuffers);
			}
			
			// this is the right place for a data-safety-securing force/flush.
			channel.force(false);
			
			return file.length() - oldLength;
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
		return this.copy(sourceFile, 0, sourceFile.length(), targetfile);
	}

	public default long copy(
		final StorageLockedFile sourceFile  ,
		final long              sourceOffset,
		final long              length      ,
		final StorageLockedFile targetfile
	)
	{
//		DEBUGStorage.println("storage copy file range");

		try
		{
			final long byteCount = sourceFile.channel().transferTo(sourceOffset, length, targetfile.channel());
			targetfile.channel().force(false);
			
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
	 * @param length
	 * @param targetfile
	 * @return
	 */
	public default long writeImport(
		final StorageLockedFile  sourceFile  ,
		final long               sourceOffset,
		final long               length      ,
		final StorageDataFile<?> targetfile
	)
	{
		return this.copy(sourceFile, sourceOffset, length, targetfile);
	}
	
	public default long writeTransfer(
		final StorageDataFile<?> sourceFile  ,
		final long               sourceOffset,
		final long               length      ,
		final StorageDataFile<?> targetfile
	)
	{
		return this.copy(sourceFile, sourceOffset, length, targetfile);
	}
	
	public default long writeTransactionEntryCreate(
		final StorageInventoryFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default long writeTransactionEntryStore(
		final StorageInventoryFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile       ,
		final long                     dataFileOffset ,
		final long                     storeLength
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

	public default void truncate(final StorageInventoryFile file, final long newLength)
	{
//		DEBUGStorage.println("storage file truncation");

		try
		{
			file.channel().truncate(newLength);
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
		delete(file, storageFileProvider);
	}
	
	public static void delete(
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
	
	/* (25.02.2019 TM)NOTE:
	 * No idea how to cleanly call it since the deletionDirectory concept changed. Maybe superfluous after all.
	 */
//	public static void createTruncationBak(
//		final File        deletionDirectory,
//		final StorageFile file             ,
//		final long        newLength
//	)
//	{
//		final File dirBak = new File(deletionDirectory, "bak");
//
//		final String bakFileName = file.name() + "_truncated_from_" + file.length() + "_to_" + newLength
//			+ "_@" + System.currentTimeMillis() + ".bak"
//		;
////		XDebug.debugln("Creating truncation bak file: " + bakFileName);
//		XFiles.ensureDirectory(dirBak);
//		try
//		{
//			Files.copy(
//				Paths.get(file.identifier()),
//				dirBak.toPath().resolve(bakFileName)
//			);
////			XDebug.debugln("* bak file created: " + bakFileName);
//		}
//		catch(final IOException e)
//		{
//			throw new StorageExceptionIo(e); // (04.03.2015 TM)EXCP: proper exception
//		}
//	}

	public default void flush(final StorageLockedFile targetfile)
	{
		try
		{
			targetfile.channel().force(false);
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (01.10.2014)EXCP: proper exception
		}
	}

	public final class Implementation implements StorageFileWriter
	{
		// since default methods, interfaces should be directly instantiable :(
	}
	
	

	@FunctionalInterface
	public interface Provider
	{
		public StorageFileWriter provideWriter(final int channelIndex);

		public final class Implementation implements StorageFileWriter.Provider
		{
			@Override
			public StorageFileWriter provideWriter(final int channelIndex)
			{
				return new StorageFileWriter.Implementation();
			}
		}

	}
	
}
