package one.microstream.afs;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

public interface AWritableFile extends AReadableFile
{
	public void openWriting();
	
	public boolean isOpenWriting();
	
	// ONLY the writing IO-Aspect, not the AFS-management-level aspect. Reading aspect remains open.
	public void closeWriting();
	
	public boolean isClosedWriting();

	// implicitely #closeWriting PLUS the AFS-management-level WRITING aspect. BOTH reading aspects remain!
	public boolean releaseWriting();
	
	@Override
	public boolean release();
	
	
	public long write(Iterable<? extends ByteBuffer> sources);
	
	
	
	public static AWritableFile New(
		final AccessManager accessManager,
		final AFile         actual
	)
	{
		return new AWritableFile.Default(
			notNull(accessManager),
			AFile.actual(actual) // just to be sure/safe
		);
	}
		
	public final class Default extends AReadableFile.Default implements AWritableFile
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final AccessManager accessManager, final AFile actual)
		{
			super(accessManager, actual);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void openWriting()
		{
			// (29.04.2020 TM)FIXME: priv#49: AWritableFile#openWriting()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean isOpenWriting()
		{
			// (29.04.2020 TM)FIXME: priv#49: AWritableFile#isOpenWriting()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public void closeWriting()
		{
			// (29.04.2020 TM)FIXME: priv#49: AWritableFile#closeWriting()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean isClosedWriting()
		{
			// (29.04.2020 TM)FIXME: priv#49: AWritableFile#isClosedWriting()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		@Override
		public boolean release()
		{
			// (29.04.2020 TM)FIXME: priv#49: reimplement to call #releaseWriting implicitely
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean releaseWriting()
		{
			// (29.04.2020 TM)FIXME: priv#49: AWritableFile#releaseWriting()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public long write(final Iterable<? extends ByteBuffer> sources)
		{
			// (29.04.2020 TM)FIXME: priv#49: AWritableFile#write()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
	}
			
}
