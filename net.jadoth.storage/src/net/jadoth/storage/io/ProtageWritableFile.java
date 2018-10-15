package net.jadoth.storage.io;

public interface ProtageWritableFile extends ProtageReadableFile
{
	@Override
	public ProtageWritableDirectory directory();
	
	public ProtageWritingFileChannel createWritingChannel();
	
	@Override
	public default int activeChannels()
	{
		synchronized(this)
		{
			return this.activeReadingChannels() + this.activeWritingChannels();
		}
	}

	public int activeWritingChannels();
	
	/**
	 * Attempts to delete the file.
	 * If any amount of {@link ProtageFileChannel} instances are still actively accessing the file,
	 * it is neither deleted nor marked for deletion.
	 * 
	 * @return the amount of still actively accessing {@link ProtageFileChannel} instances.
	 */
	public int tryDelete();
	
	/**
	 * {@link #tryDelete} with mark for deletion if not possible.
	 * @return
	 */
	public int delete();
	
	
	/**
	 * Definitely delete now, close all channels, or throw exception if not possible.
	 * @throws RuntimeException
	 */
	public void forceDelete() throws RuntimeException; // (15.10.2018 TM)EXCP: proper exception

	
	public boolean isMarkedForDeletion();
}
