package one.microstream.afs;

import static one.microstream.X.notNull;

import one.microstream.collections.types.XGettingTable;

public interface AUsedDirectory extends ADirectory, ADirectory.Wrapper
{
	public boolean release();
	
	
	
	public static AUsedDirectory New(
		final AccessManager accessManager,
		final ADirectory    actual
	)
	{
		return new AUsedDirectory.Default(
			notNull(accessManager),
			ADirectory.actual(actual) // just to be sure/safe
		);
	}
	
	public class Default implements AUsedDirectory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final AccessManager accessManager;
		private final ADirectory    actual       ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final AccessManager accessManager, final ADirectory actual)
		{
			super();
			this.accessManager = accessManager;
			this.actual        = actual       ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ADirectory actual()
		{
			return this.actual;
		}

		@Override
		public XGettingTable<String, ? extends ADirectory> directories()
		{
			return this.actual.directories();
		}

		@Override
		public XGettingTable<String, ? extends AFile> files()
		{
			return this.actual.files();
		}

		@Override
		public boolean registerObserver(final Observer observer)
		{
			return this.actual.registerObserver(observer);
		}

		@Override
		public boolean removeObserver(final Observer observer)
		{
			return this.actual.removeObserver(observer);
		}

		@Override
		public ADirectory parent()
		{
			return this.actual.parent();
		}

		@Override
		public String path()
		{
			return this.actual.path();
		}

		@Override
		public String identifier()
		{
			return this.actual.identifier();
		}

		@Override
		public boolean exists()
		{
			return this.actual.exists();
		}

		@Override
		public boolean release()
		{
			// (30.04.2020 TM)FIXME: priv#49: #release
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
	}
	
}
