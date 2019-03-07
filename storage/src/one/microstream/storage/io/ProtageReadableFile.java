package one.microstream.storage.io;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public interface ProtageReadableFile extends ProtageFile
{
	@Override
	public ProtageReadableDirectory directory();
	
	public void open();
	
	public boolean isOpen();
		
	public void close();
	
	public boolean isClosed();
		
	public <C extends Consumer<? super ProtageReadableFile>> C waitOnClose(C callback);
	
	public abstract long read(ByteBuffer target, long position);
	
	public default void copyTo(final ProtageWritableFile target)
	{
		// must lock here to get a reliable length value
		synchronized(this)
		{
			this.copyTo(target, 0, this.length());
		}
	}

	public default void copyTo(final ProtageWritableFile target, final long sourcePosition, final long length)
	{
		/* (29.10.2018 TM)FIXME: OGS-45: default copyTo
		 * create a special Iterator that (re)fills its buffer on every next() call for the whole length
		 */
		if(System.currentTimeMillis() > 0)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME ProtageReadableFile#copyTo()
		}
		synchronized(this)
		{
			final Iterable<ByteBuffer> shovel = null;
			target.write(shovel);
		}
	}
	
}
