package net.jadoth.storage.types;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


/**
 * Function type that encapsulates handling of all writing accesses to persistent data, including copying,
 * truncation, deletion.
 *
 * @author TM
 */
public interface StorageFileWriter
{
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

	
	// (13.02.2019 TM)NOTE: single ByteBuffer variant removed to keep implementations simple.
	
	public default long writeStore(
		final StorageDataFile<?> targetFile ,
		final ByteBuffer[]       byteBuffers
	)
	{
		return this.write(targetFile, byteBuffers);
	}
	
	public default long writeTransactionEntryStore(
		final StorageLockedChannelFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile       ,
		final long                     dataFileOffset ,
		final long                     storeLength
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default long writeTransactionEntryTransfer(
		final StorageLockedChannelFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile       ,
		final long                     dataFileOffset ,
		final long                     storeLength
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default long writeTransactionEntryDelete(
		final StorageLockedChannelFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile
	)
	{
		return this.write(transactionFile, byteBuffers);
	}
	
	public default long writeTransactionEntryCreate(
		final StorageLockedChannelFile transactionFile,
		final ByteBuffer[]             byteBuffers    ,
		final StorageDataFile<?>       dataFile
	)
	{
		return this.write(transactionFile, byteBuffers);
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

	public default long copy(
		final StorageFile       sourceFile,
		final StorageLockedFile targetfile
	)
	{
		return this.copy(sourceFile, targetfile, 0, sourceFile.length());
	}

	public default long copy(
		final StorageFile       sourceFile  ,
		final StorageLockedFile targetfile  ,
		final long              sourceOffset,
		final long              length
	)
	{
//		DEBUGStorage.println("storage copy file range");

		try
		{
			final long byteCount = sourceFile.channel().transferTo(sourceOffset, length, targetfile.channel());
			targetfile.channel().force(false);
			return byteCount;
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (01.10.2014)EXCP: proper exception
		}
	}
	
	public default long writeTransfer(
		final StorageDataFile<?> sourceFile  ,
		final StorageDataFile<?> targetfile  ,
		final long               sourceOffset,
		final long               length
	)
	{
		return this.copy(sourceFile, targetfile, sourceOffset, length);
	}

	public default void truncate(final StorageLockedFile file, final long newLength)
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

	public default void delete(final StorageLockedChannelFile file)
	{
//		DEBUGStorage.println("storage file deletion");

		if(file.delete())
		{
			return;
		}
		
		throw new RuntimeException("Could not delete file " + file); // (02.10.2014 TM)EXCP: proper exception
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
