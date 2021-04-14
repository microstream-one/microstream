package one.microstream.persistence.binary.types;

import java.nio.ByteBuffer;


public interface Chunk
{
	public ByteBuffer[] buffers();

	public void clear();
	
	public boolean isEmpty();
				
	public long totalLength();
	
}
