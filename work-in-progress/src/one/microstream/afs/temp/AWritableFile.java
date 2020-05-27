package one.microstream.afs.temp;

import java.nio.ByteBuffer;

public interface AWritableFile extends AReadableFile
{
	/* (24.05.2020 TM)TODO: priv#49 releasePrivileged?
	 * Idea:
	 * The "main" user/owner of a file can release its hold on the file, e.g. caused by a timeout,
	 * and thus allow others to access it, BUT if it needs its access back, it can override meanwhile users
	 * and force it back.
	 * Of course, the currently ongoing operation of another user would have to be allowed to complete and then
	 * notify the waiting main user/owner.
	 * Or maybe this is just on overcomplication and the simple solution for that is:
	 * Both must be the same user (maybe shared accross different threads), but managed on
	 * the application's responsibility.
	 */
	
	@Override
	public default boolean open()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().openWriting(this);
	}
		
	public default long writeBytes(final Iterable<? extends ByteBuffer> sources)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().writeBytes(this, sources);
	}

	public default boolean create()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().create(this);
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
