package one.microstream.afs;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public interface AReadableFile extends AFile, AFile.Wrapper
{
	public void open();
	
	public boolean isOpen();
	
	// ONLY the IO-Aspect, not the AFS-management-level aspect
	public void close();
	
	public boolean isClosed();

	// implicitely #close PLUS the AFS-management-level aspect
	public boolean release();
	
	
	
	// (28.04.2020 TM)FIXME: priv#49: review old stuff below
		
	public <C extends Consumer<? super AReadableFile>> C waitOnClose(C callback);
	
	public abstract long read(ByteBuffer target, long position);
	
	public default void copyTo(final AWritableFile target)
	{
		// must lock here to get a reliable length value
		synchronized(this)
		{
			this.copyTo(target, 0, this.length());
		}
	}


	// (28.04.2020 TM)FIXME: priv#49: review/overhaul/delete
	public default void copyTo(final AWritableFile target, final long sourcePosition, final long length)
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
