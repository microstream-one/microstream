package one.microstream.afs;

import static one.microstream.X.notNull;

public interface AMutableDirectory extends AUsedDirectory
{
	public AMutableDirectory move(AFile file, AMutableDirectory destination);
	

	public boolean releaseMutating();
	
	// (29.04.2020 TM)FIXME: priv#49: reimplement to call #releaseMutating implicitely
	@Override
	public boolean release();
	
	

	
	public static AMutableDirectory New(
		final AFileSystem fileSystem,
		final ADirectory  actual
	)
	{
		return new AMutableDirectory.Default(
			notNull(fileSystem),
			ADirectory.actual(actual) // just to be sure/safe
		);
	}
	
	public class Default extends AUsedDirectory.Default implements AMutableDirectory
	{
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final AFileSystem fileSystem, final ADirectory actual)
		{
			super(fileSystem, actual);
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
		public boolean releaseMutating()
		{
			// (30.04.2020 TM)FIXME: priv#49: #releaseMutating
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public AMutableDirectory move(final AFile file, final AMutableDirectory destination)
		{
			// (30.04.2020 TM)FIXME: priv#49: #move
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
	}
	
}
