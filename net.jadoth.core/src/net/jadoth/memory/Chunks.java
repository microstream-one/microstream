package net.jadoth.memory;

import java.nio.ByteBuffer;


public interface Chunks
{
	public ByteBuffer[] buffers();

	public void clear();
	
	public boolean isEmpty();
	
	public long entityCount();
}
