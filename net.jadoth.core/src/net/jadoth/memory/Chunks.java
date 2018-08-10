package net.jadoth.memory;

import java.nio.ByteBuffer;


public interface Chunks
{
	public ByteBuffer[] buffers();

	public void clear();
	
	public boolean isEmpty();
			
	/* (10.08.2018 TM)TODO: long totalLength() is required, e.g. to easily write a chunks header
	 * Should be an inherent value that is updated on the fly instead of lazily calculated.
	 * Much more useful than keeping the entity count up to date ...
	 */
	
}
