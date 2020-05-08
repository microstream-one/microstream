package one.microstream.afs;

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
	
	

	// (07.05.2020 TM)FIXME: priv#49: must be implementation detail of FileSystem or such
//	public static AWritableFile New(
//		final AFile       actual
//	)
//	{
//		return new AWritableFile.Default(
//			AFile.actual(actual) // just to be sure/safe
//		);
//	}
		
	public final class Default<U, S> extends AReadableFile.Default<U, S> implements AWritableFile
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final AFile actual, final U user, final S subject)
		{
			super(actual, user, subject);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
	

		@Override
		public void openWriting()
		{
			// (29.04.2020 TM)FIXME: priv#49: AWritableFile#openWriting()
			this.fileSystem();
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean isOpenWriting()
		{
			// (29.04.2020 TM)FIXME: priv#49: AWritableFile#isOpenWriting()
			this.fileSystem();
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public void closeWriting()
		{
			// (29.04.2020 TM)FIXME: priv#49: AWritableFile#closeWriting()
			this.fileSystem();
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean isClosedWriting()
		{
			// (29.04.2020 TM)FIXME: priv#49: AWritableFile#isClosedWriting()
			this.fileSystem();
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		@Override
		public boolean release()
		{
			// (29.04.2020 TM)FIXME: priv#49: reimplement to call #releaseWriting implicitely
			this.fileSystem();
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean releaseWriting()
		{
			// (29.04.2020 TM)FIXME: priv#49: AWritableFile#releaseWriting()
			this.fileSystem();
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public long write(final Iterable<? extends ByteBuffer> sources)
		{
			// (29.04.2020 TM)FIXME: priv#49: AWritableFile#write()
			this.fileSystem();
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
	}
			
}
