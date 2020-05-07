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


	
	public default void copyTo(final AWritableFile target, final long sourcePosition, final long length)
	{
		/* (29.10.2018 TM)FIXME: priv#49: default copyTo
		 * create a special Iterator that (re)fills its buffer on every next() call for the whole length
		 * (28.04.2020 TM): review/overhaul/delete
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
	
	
	
	public static AReadableFile New(
		final AFile       actual
	)
	{
		return new AReadableFile.Default(
			AFile.actual(actual) // just to be sure/safe
		);
	}
	
	public class Default extends AFile.Wrapper.Abstract implements AReadableFile
	{
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default( final AFile actual)
		{
			super(actual);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public boolean release()
		{
			// (30.04.2020 TM)FIXME: priv#49: #release
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public long length()
		{
			// (30.04.2020 TM)FIXME: priv#49: AFile#length()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public void open()
		{
			// (30.04.2020 TM)FIXME: priv#49: AReadableFile#open()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean isOpen()
		{
			// (30.04.2020 TM)FIXME: priv#49: AReadableFile#isOpen()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public void close()
		{
			// (30.04.2020 TM)FIXME: priv#49: AReadableFile#close()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean isClosed()
		{
			// (30.04.2020 TM)FIXME: priv#49: AReadableFile#isClosed()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public <C extends Consumer<? super AReadableFile>> C waitOnClose(final C callback)
		{
			// (30.04.2020 TM)FIXME: priv#49: AReadableFile#waitOnClose()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public long read(final ByteBuffer target, final long position)
		{
			// (30.04.2020 TM)FIXME: priv#49: AReadableFile#read()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
	}
	
}
