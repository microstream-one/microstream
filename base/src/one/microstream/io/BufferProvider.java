package one.microstream.io;

import java.nio.ByteBuffer;

public interface BufferProvider
{
	public void initializeOperation();
	
	public ByteBuffer provideBuffer();
	
	public ByteBuffer provideBuffer(long size);
	
	public void completeOperation();
}
