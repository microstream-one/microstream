package one.microstream.afs;

import java.nio.ByteBuffer;

public interface AWritableFile extends AReadableFile
{
	public default boolean openWriting()
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().openWriting(this);
		}
	}
	
	public default boolean isOpenWriting()
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().isOpenWriting(this);
		}
	}
	
	// ONLY the writing IO-Aspect, not the AFS-management-level aspect. Reading aspect remains open.
	public default boolean closeWriting()
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().closeWriting(this);
		}
	}
	
	public default boolean isClosedWriting()
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().isClosedWriting(this);
		}
	}

	// implicitely #closeWriting PLUS the AFS-management-level WRITING aspect. BOTH reading aspects remain!
	public default boolean releaseWriting()
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().releaseWriting(this);
		}
	}
	
	public default long writeBytes(final Iterable<? extends ByteBuffer> sources)
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().writeBytes(this, sources);
		}
	}
	
	
		
	public final class Default<U, S> extends AReadableFile.Default<U, S> implements AWritableFile
	{
		Default(final AFile actual, final U user, final S subject)
		{
			super(actual, user, subject);
		}
		
	}
			
}
