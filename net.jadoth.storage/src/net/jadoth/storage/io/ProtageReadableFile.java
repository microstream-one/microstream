package net.jadoth.storage.io;

public interface ProtageReadableFile extends ProtageFile
{
	@Override
	public ProtageReadableDirectory directory();
	
	public ProtageReadingFileChannel createReadingChannel(ProtageFileChannel.Owner owner, String name);
	
	
	
	/**
	 * Attempts to close the file.
	 * If any amount of {@link ProtageFileChannel} instances are still actively accessing the file,
	 * it is neither closed nor marked for closing.
	 * 
	 * @return the amount of still actively accessing {@link ProtageFileChannel} instances.
	 */
	public int tryClose();
	
	// (16.10.2018 TM)TODO: OGS-45: Is a pending close really necessary? What for?
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
	
	public default boolean isClosed()
	{
		return this.activeChannels() <= 0;
	}
	
	public default int activeChannels()
	{
		return this.activeReadingChannels();
	}
	
	public int activeReadingChannels();
		
	public void copyTo(ProtageWritableFile target);

	public void copyTo(ProtageWritableFile target, long sourcePosition, long sourceLength);
	
	public void moveTo(ProtageWritableDirectory destination);
	
}
