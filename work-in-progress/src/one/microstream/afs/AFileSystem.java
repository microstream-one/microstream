package one.microstream.afs;

import static one.microstream.X.notNull;

import one.microstream.collections.EqHashTable;

public interface AFileSystem<D, F> extends AccessManager<D, F>
{
	// (24.04.2020 TM)FIXME: priv#49: Shouldn't this be a specialized "AResolver" known a properly abstract FileSystem?
	
	public ADirectory resolveDirectory(D directory);
	
	public AFile resolveFile(F file);

	
	public abstract class Abstract<
		SD,
		SF,
		D extends ADirectory,
		R extends AReadableFile,
		W extends AWritableFile,
		F extends AFile.AbstractRegistering<SF, D, R, W>
	>
		implements AFileSystem<SD, SF>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final EqHashTable<String, D> rootDirectories;
		private final AccessManager<SD, SF>  accessManager  ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(final AccessManager<SD, SF> accessManager)
		{
			super();
			this.rootDirectories = EqHashTable.New();
			this.accessManager   = notNull(accessManager);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public ADirectory resolveDirectory(final SD directory)
		{
			// (24.04.2020 TM)FIXME: priv#49: AFileSystem.Abstract#resolveDirectory()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
		@Override
		public AFile resolveFile(final SF file)
		{
			// (24.04.2020 TM)FIXME: priv#49: AFileSystem.Abstract#resolveFile()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public AReadableFile createDirectory(final AMutableDirectory parent, final SD directory)
		{
			return this.accessManager.createDirectory(parent, directory);
		}
		
		@Override
		public AReadableFile createFile(final AMutableDirectory parent, final SF file)
		{
			return this.accessManager.createFile(parent, file);
		}
		
		@Override
		public AReadableFile useReading(final AFile file, final Object reader)
		{
			return this.accessManager.useReading(file, reader);
		}
		
		@Override
		public AWritableFile useWriting(final AFile file, final Object writer)
		{
			return this.accessManager.useWriting(file, writer);
		}
		
		@Override
		public AMutableDirectory useMutating(final ADirectory directory, final Object mutator)
		{
			return this.accessManager.useMutating(directory, mutator);
		}
		
	}
	
}
