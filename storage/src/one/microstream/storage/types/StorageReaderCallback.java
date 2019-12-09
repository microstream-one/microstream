package one.microstream.storage.types;

import java.io.IOException;
import java.nio.ByteBuffer;

import one.microstream.storage.exceptions.StorageException;

public interface StorageReaderCallback
{
	public void validateIncrementalRead(
		StorageLockedFile file         ,
		long              filePosition ,
		ByteBuffer        buffer       ,
		long              lastReadCount
	)
		throws IOException
	;
	
	public static void staticValidateIncrementalRead(
		final StorageLockedFile file         ,
		final long              filePosition ,
		final ByteBuffer        buffer       ,
		final long              lastReadCount
	)
		throws IOException
	{
		if(lastReadCount < 0)
		{
			// (30.06.2013 TM)EXCP: proper exception
			throw new StorageException(
				"Could not read data in file " +file.identifier()+ " at position " + filePosition
			);
		}
	}
	
	public static StorageReaderCallback Default()
	{
		return new StorageReaderCallback.Default();
	}
	
	public final class Default implements StorageReaderCallback
	{

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
		
	}
}
