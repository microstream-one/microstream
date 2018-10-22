package net.jadoth.storage.io;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public interface ProtageReadableFile extends ProtageFile
{
	@Override
	public ProtageReadableDirectory directory();
	
	public void open();
	
	public boolean isOpen();
		
	
	
	/**
	 * Attempts to close the file.
	 * If any amount of {@link ProtageFileChannel} instances are still actively accessing the file,
	 * it is neither closed nor marked for closing.
	 * 
	 * @return the amount of still actively accessing {@link ProtageFileChannel} instances.
	 */
	public int tryClose();
	
	/**
	 * {@link #tryClose} with mark for closing if not possible.
	 * @return
	 */
	public int close();
	
	/**
	 * Definitely close now, close all channels, or throw exception if not possible.
	 * @return the number of channels that have been closed by execution the method.
	 * @throws RuntimeException
	 */
	public int forceClose() throws RuntimeException; // (15.10.2018 TM)EXCP: proper exception
	
	public boolean isClosed();
		
	public <C extends Consumer<? super ProtageReadableFile>> C waitOnClose(C callback);
	
	public abstract long read(ByteBuffer target, long position);
	
	public void copyTo(ProtageWritableFile target);

	public void copyTo(ProtageWritableFile target, long sourcePosition, long length);
	
}
