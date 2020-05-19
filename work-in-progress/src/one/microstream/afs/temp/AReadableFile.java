package one.microstream.afs.temp;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

import one.microstream.io.BufferProvider;

public interface AReadableFile extends AFile.Wrapper
{
	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public default boolean open()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().openReading(this);
	}

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public default boolean isOpen()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().isOpenReading(this);
	}
	
	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public default boolean close()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().close(this);
	}

	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public default boolean isClosed()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().isClosed(this);
	}

	// implicitely #close PLUS the AFS-management-level aspect
	public default ActionReport release()
	{
		// synchronization handled by FileSystem.
		return this.actual().fileSystem().release(this);
	}
	

	public default boolean ensure()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().ensure(this);
	}
	
	@Override
	public default long length()
	{
		// synchronization handled by IoHandler.
		return this.fileSystem().ioHandler().length(this);
	}
	
			

	public default ByteBuffer readBytes()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this);
	}
	
	public default ByteBuffer readBytes(final long position)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, position);
	}
	
	public default ByteBuffer readBytes(final long position, final long length)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, position, length);
	}
	
	
	public default long readBytes(final ByteBuffer targetBuffer)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, targetBuffer);
	}
	
	public default long readBytes(final ByteBuffer targetBuffer, final long position)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, targetBuffer, position);
	}
	
	public default long readBytes(final ByteBuffer targetBuffer, final long position, final long length)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, targetBuffer, position, length);
	}
	
	
	public default long readBytes(final BufferProvider bufferProvider)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, bufferProvider);
	}
	
	public default long readBytes(final BufferProvider bufferProvider, final long position)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, bufferProvider, position);
	}
	
	public default long readBytes(final BufferProvider bufferProvider, final long position, final long length)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().readBytes(this, bufferProvider, position, length);
	}

	
	
	public default long copyTo(final AWritableFile target)
	{
		return this.actual().fileSystem().ioHandler().copyTo(this, target);
	}
	
	public default long copyTo(final AWritableFile target, final long sourcePosition)
	{
		return this.actual().fileSystem().ioHandler().copyTo(this, target, sourcePosition);
	}
	
	public default long copyTo(final AWritableFile target, final long sourcePosition, final long length)
	{
		return this.actual().fileSystem().ioHandler().copyTo(this, target, sourcePosition, length);
	}
	
	
	
	public static AReadableFile New(
		final AFile  actual ,
		final Object user   ,
		final Object subject
	)
	{
		return new AReadableFile.Default<>(
			notNull(actual) ,
			notNull(user)   ,
			notNull(subject)
		);
	}
	
	public class Default<U, S> extends AFile.Wrapper.Abstract<U, S> implements AReadableFile
	{
		Default(final AFile actual, final U user, final S subject)
		{
			super(actual, user, subject);
		}
				
	}
	
}
