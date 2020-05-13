package one.microstream.afs;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

public interface AWritableFile extends AReadableFile
{
	@Override
	public default boolean open()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().openWriting(this);
	}
	
	@Override
	public default boolean isOpen()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().isOpenWriting(this);
	}
		
	public default long writeBytes(final Iterable<? extends ByteBuffer> sources)
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().writeBytes(this, sources);
	}
	
	public default boolean delete()
	{
		// synchronization handled by IoHandler.
		return this.actual().fileSystem().ioHandler().deleteFile(this);
	}
	
	
	
	
	public static AWritableFile New(
		final AFile  actual ,
		final Object user   ,
		final Object subject
	)
	{
		return new AWritableFile.Default<>(
			notNull(actual) ,
			notNull(user)   ,
			notNull(subject)
		);
	}
		
	public class Default<U, S> extends AReadableFile.Default<U, S> implements AWritableFile
	{
		Default(final AFile actual, final U user, final S subject)
		{
			super(actual, user, subject);
		}
		
	}
			
}
