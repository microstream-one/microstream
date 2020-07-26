package one.microstream.storage.types;

import static one.microstream.X.notNull;

import one.microstream.afs.AFile;

public interface StorageLockFile extends StorageClosableFile
{
	public static StorageLockFile New(final AFile file)
	{
		return new StorageLockFile.Default(
			notNull(file)
		);
	}
	
	public final class Default extends StorageFile.Abstract implements StorageLockFile
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(final AFile file)
		{
			super(file);
		}
				
	}
	
}
