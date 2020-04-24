package one.microstream.afs;

public interface AMutableDirectory extends ADirectory, ADirectory.Wrapper
{
	// (21.04.2020 TM)FIXME: priv#49: overhaul with new concept

	public AMutableDirectory move(AFile file, AMutableDirectory destination);
	
	
	
	public static <M extends AMutableDirectory> Entry<M> Entry()
	{
		return new Entry.Default<>();
	}
	
	public interface Entry<M extends AMutableDirectory>
	{
		public Object mutator();
		
		public M directory();

		public Object setMutator(Object writer);
		
		public M setDirectory(M file);
		
		static final class Default<W extends AMutableDirectory> implements AMutableDirectory.Entry<W>
		{
			private Object mutator  ;
			private W      directory;
			
			Default()
			{
				super();
			}

			@Override
			public final Object mutator()
			{
				return this.mutator;
			}

			@Override
			public final W directory()
			{
				return this.directory;
			}

			@Override
			public final Object setMutator(final Object mutator)
			{
				final Object oldMutator = this.mutator;
				this.mutator = mutator;
				
				return oldMutator;
			}

			@Override
			public final W setDirectory(final W directory)
			{
				final W oldDirectory = this.directory;
				this.directory = directory;
				
				return oldDirectory;
			}
			
		}
		
	}
}
