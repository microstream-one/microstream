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
	
	public default long copyFrom(final AReadableFile source)
	{
		return this.actual().fileSystem().ioHandler().copyTo(source, this);
	}
	
	public default long copyFrom(final AReadableFile source, final long sourcePosition)
	{
		return this.actual().fileSystem().ioHandler().copyTo(source, sourcePosition, this);
	}

	public default long copyFrom(final AReadableFile source, final long sourcePosition, final long length)
	{
		return this.actual().fileSystem().ioHandler().copyTo(source, sourcePosition, length, this);
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
	
	public default boolean moveTo(final AWritableFile targetFile)
	{
		this.actual().fileSystem().ioHandler().moveFile(this, targetFile);
		
		// hardly reasonable to pass through the return value since it must always be true; maybe for bug hunting.
		return this.release();
	}
	
	public default AReadableFile downgrade()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().accessManager().downgrade(this);
	}
	
	public default void truncate(final long newSize)
	{
		// synchronization handled by IoHandler.
		this.actual().fileSystem().ioHandler().truncate(this, newSize);
	}
	
	/* (03.06.2020 TM)FIXME: priv#49: rename file
	 * including physical file, if exists.
	 */
					
}
