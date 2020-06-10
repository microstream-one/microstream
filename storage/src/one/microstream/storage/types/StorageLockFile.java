package one.microstream.storage.types;

import static one.microstream.X.notNull;

import one.microstream.afs.AFile;

public interface StorageLockFile extends StorageFile
{
	// (10.06.2020 TM)FIXME: priv#49: StorageLockFile
	
	
	
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
