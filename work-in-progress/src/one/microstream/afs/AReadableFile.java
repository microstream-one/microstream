package one.microstream.afs;

import java.nio.ByteBuffer;

import one.microstream.io.BufferProvider;

public interface AReadableFile extends AFile.Wrapper
{
	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public default boolean open()
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().open(this);
		}
	}

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public default boolean isOpen()
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().isOpen(this);
		}
	}
	
	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public default boolean close()
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().close(this);
		}
	}

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public default boolean isClosed()
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().isClosed(this);
		}
	}


	// (10.05.2020 TM)FIXME: priv#49: enum for quad-state return value? Use for dual-value as well?
	// implicitely #close PLUS the AFS-management-level aspect
	public default boolean release()
	{
		synchronized(this)
		{
			this.actual().fileSystem().ioHandler().close(this);
			
			return this.actual().fileSystem().accessManager().release(this);
		}
	}
	

	public default boolean ensure()
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().ensure(this);
		}
	}
	
	@Override
	default long length()
	{
		synchronized(this)
		{
			return this.fileSystem().ioHandler().length(this);
		}
	}
	
			

	public default ByteBuffer readBytes()
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().readBytes(this);
		}
	}
	
	public default ByteBuffer readBytes(final long position)
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().readBytes(this, position);
		}
	}
	
	public default ByteBuffer readBytes(final long position, final long length)
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().readBytes(this, position, length);
		}
	}
	
	
	public default long readBytes(final ByteBuffer targetBuffer)
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().readBytes(this, targetBuffer);
		}
	}
	
	public default long readBytes(final ByteBuffer targetBuffer, final long position)
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().readBytes(this, targetBuffer, position);
		}
	}
	
	public default long readBytes(final ByteBuffer targetBuffer, final long position, final long length)
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().readBytes(this, targetBuffer, position, length);
		}
	}
	
	
	public default long readBytes(final BufferProvider bufferProvider)
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().readBytes(this, bufferProvider);
		}
	}
	
	public default long readBytes(final BufferProvider bufferProvider, final long position)
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().readBytes(this, bufferProvider, position);
		}
	}
	
	public default long readBytes(final BufferProvider bufferProvider, final long position, final long length)
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().readBytes(this, bufferProvider, position, length);
		}
	}

	
	
	public default long copyTo(final AWritableFile target)
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().copyTo(this, target);
		}
	}
	
	public default long copyTo(final AWritableFile target, final long sourcePosition)
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().copyTo(this, target, sourcePosition);
		}
	}
	
	public default long copyTo(final AWritableFile target, final long sourcePosition, final long length)
	{
		synchronized(this)
		{
			return this.actual().fileSystem().ioHandler().copyTo(this, target, sourcePosition, length);
		}
	}
	
	
	
	public class Default<U, S> extends AFile.Wrapper.Abstract<U, S> implements AReadableFile
	{
		Default(final AFile actual, final U user, final S subject)
		{
			super(actual, user, subject);
		}
				
	}
	
}
