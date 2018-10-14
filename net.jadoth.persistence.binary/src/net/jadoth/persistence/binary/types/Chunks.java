package net.jadoth.persistence.binary.types;

import java.nio.ByteBuffer;


public interface Chunks
{
	public ByteBuffer[] buffers();

	public void clear();
	
	public boolean isEmpty();
				
	public long totalLength();
	
}
