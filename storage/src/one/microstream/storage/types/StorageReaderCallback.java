package one.microstream.storage.types;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface StorageReaderCallback
{
	public long incrementalRead(final StorageLockedFile file, long filePosition, ByteBuffer buffer, long lastReadCount)
		throws IOException;
}
