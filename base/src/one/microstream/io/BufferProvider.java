package one.microstream.io;

import java.nio.ByteBuffer;

public interface BufferProvider
{
	public void initializeOperation();
	
	public ByteBuffer provideNextBuffer();
	
	public void completeOperation();
}
