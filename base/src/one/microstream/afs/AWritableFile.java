package one.microstream.afs;

import java.nio.ByteBuffer;

import one.microstream.X;

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
	
	@Override
	public default long copyTo(final AWritableFile target)
	{
		return this.actual().fileSystem().ioHandler().copyTo(this, target);
	}
	
	@Override
	public default long copyTo(final AWritableFile target, final long sourcePosition)
	{
		return this.actual().fileSystem().ioHandler().copyTo(this, sourcePosition, target);
	}

	@Override
	public default long copyTo(final AWritableFile target, final long sourcePosition, final long length)
	{
		return this.actual().fileSystem().ioHandler().copyTo(this, sourcePosition, length, target);
	}
	
	public default long copyFrom(final AReadableFile source)
	{
		return this.actual().fileSystem().ioHandler().copyFrom(source, this);
	}
	
	public default long copyFrom(final AReadableFile source, final long sourcePosition)
	{
		return this.actual().fileSystem().ioHandler().copyFrom(source, sourcePosition, this);
	}

	public default long copyFrom(final AReadableFile source, final long sourcePosition, final long length)
	{
		return this.actual().fileSystem().ioHandler().copyFrom(source, sourcePosition, length, this);
	}
	
	public default long writeBytes(final ByteBuffer source)
	{
		return this.writeBytes(X.Constant(source));
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
		
	@Override
	public default boolean ensureExists()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().ensureExists(this);
	}
	
	public default boolean delete()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().deleteFile(this);
		
		/* note:
		 * no release since an abstract file represents "the notion of a physical file".
		 * It can remain valid even after the physical file was removed.
		 */
	}
	
	public default void moveTo(final ADirectory targetDirectory)
	{
		final AFile targetFile = targetDirectory.ensureFile(this.identifier(), this.name(), this.type());
		
		final AWritableFile wFile = targetFile.useWriting();
		try
		{
			this.moveTo(wFile);
		}
		finally
		{
			wFile.release();
		}
	}
	
	public default void moveTo(final AWritableFile targetFile)
	{
		// synchronization handled by IoHandler.
		this.actual().fileSystem().ioHandler().moveFile(this, targetFile);
		
		/* note:
		 * no release since an abstract file represents "the notion of a physical file".
		 * It can remain valid even after the physical file was removed.
		 */
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
	
	/* (03.06.2020 TM)TODO: priv#49: rename file
	 * including physical file, if exists.
	 * 
	 * (19.07.2020 TM):
	 * This is tricky:
	 * Since AFile instances are immutable singletons that abstractly represent files,
	 * the name cannot simply be changed directly.
	 * An AFile is not an identity in itself with a mutable name, but the name defines the identity.
	 * So another name means another AFile instance.
	 * Renaming the physical file just shifts it from "belonging" to the new AFile instance instead of the
	 * old one.
	 * 
	 * So the algorithm must be:
	 * - Create and register new AFile instance
	 * - Rename physical file
	 * - Rollback on exception
	 * - Release and retire old AWritableFile instance
	 * - Unregister old AFile instance
	 * - Create new AWritableFile instance.
	 * - Return new AWritableFile
	 * Of course, everything under a central lock in the FileSystem
	 * 
	 * Downgraded to T0D0 since MicroStream does not require file renaming.
	 */
					
}
