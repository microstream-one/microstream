package one.microstream.afs;

import java.nio.ByteBuffer;

public interface AWritableFile extends AReadableFile
{
	public long write(Iterable<? extends ByteBuffer> sources);
			
}
