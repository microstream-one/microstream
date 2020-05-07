package one.microstream.afs;

import static one.microstream.X.notNull;

public interface AUsedDirectory extends ADirectory, ADirectory.Wrapper
{
	public boolean release();
	
	// (06.05.2020 TM)FIXME: priv#49: Directory accessing must be only implicitly, not explicitely
	
	
	public static AUsedDirectory New(
		final AFileSystem fileSystem,
		final ADirectory  actual
	)
	{
		return new AUsedDirectory.Default(
			notNull(fileSystem),
			ADirectory.actual(actual) // just to be sure/safe
		);
	}
	
	public class Default extends ADirectory.Wrapper.Abstract implements AUsedDirectory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final AFileSystem fileSystem;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final AFileSystem fileSystem, final ADirectory actual)
		{
			super(actual);
			this.fileSystem = fileSystem;
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
		
	}
	
}
