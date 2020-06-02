package one.microstream.afs;

import java.nio.ByteBuffer;

public interface AWritableFile extends AReadableFile
{
	@Override
	public default boolean open()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().openWriting(this);
	}
	
	/* (31.05.2020 TM)NOTE: shortcut implementations for useReading and useWriting?
	 * But beware:
	 * - Default user is defined in the accessmanager instance, so it must be used, anyway!
	 * - retired usage/wrapper instances might be used to create new, active ones. May not be suppressed!
	 * - More special cases? Thus: worth it?
	 */
	
	@Override
	public default AWritableFile useWriting(final Object user)
	{
		return this.fileSystem().accessManager().useWriting(this, user);
	}
	
	@Override
	public default AWritableFile useWriting()
	{
		return this.fileSystem().accessManager().useWriting(this);
	}
		
	public default long writeBytes(final Iterable<? extends ByteBuffer> sources)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().writeBytes(this, sources);
	}

	public default void create()
	{
		// synchronization handled by IoHandler.
		this.actual().fileSystem().ioHandler().create(this);
	}
	
	public default boolean ensure()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().ensure(this);
	}
	
	public default boolean delete()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().deleteFile(this);
	}
	
	public default AReadableFile downgrade()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().accessManager().downgrade(this);
	}
					
}
