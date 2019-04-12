package one.microstream.storage.types;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public interface StorageFileReader
{
	public default long readStorage(
		final StorageLockedFile     file                 ,
		final long                  filePosition         ,
		final ByteBuffer            targetBuffer         ,
		final StorageReaderCallback incompleteReadCallack
	)
	{
		final FileChannel fileChannel = file.fileChannel();

		try
		{
			long currentFilePosition = filePosition;
			
			// single call should normally be sufficient
			long readCount = fileChannel.read(targetBuffer, currentFilePosition);

			// if single call was not sufficient, report to callback and try incremental reads.
			while(targetBuffer.hasRemaining())
			{
				// handle all non-trivial cases (including -1 readCount) in other method to keep this one small
				incompleteReadCallack.validateIncrementalRead(file, currentFilePosition, targetBuffer, readCount);
				currentFilePosition = filePosition + targetBuffer.position();
				readCount += fileChannel.read(targetBuffer, currentFilePosition);
			}

			return readCount;
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e); // (10.12.2014 TM)EXCP: proper exception
		}
	}


	public final class Implementation implements StorageFileReader
	{
		// since default methods, interfaces should be directly instantiable :(
	}


	@FunctionalInterface
	public interface Provider
	{

		public StorageFileReader provideReader();
		
		public default StorageFileReader provideReader(final int channelIndex)
		{
			return this.provideReader();
		}

		public final class Implementation implements StorageFileReader.Provider
		{
			@Override
			public StorageFileReader provideReader()
			{
				return new StorageFileReader.Implementation();
			}
		}

	}

}
