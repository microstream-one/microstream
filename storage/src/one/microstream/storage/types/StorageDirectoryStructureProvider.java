package one.microstream.storage.types;

import one.microstream.afs.ADirectory;


public interface StorageDirectoryStructureProvider
{
	public ADirectory provideChannelDirectory(
		ADirectory              storageRootDirectory,
		int                     channelIndex        ,
		StorageFileNameProvider fileNameProvider
	);
	
	
		
	public static StorageDirectoryStructureProvider New()
	{
		return new StorageDirectoryStructureProvider.Default();
	}
	
	public final class Default implements StorageDirectoryStructureProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final ADirectory provideChannelDirectory(
			final ADirectory              storageRootDirectory,
			final int                     channelIndex        ,
			final StorageFileNameProvider fileNameProvider
		)
		{
			final String channelDirectoryName = fileNameProvider.provideChannelDirectoryName(channelIndex);
			final ADirectory channelDirectory = storageRootDirectory.ensureDirectory(channelDirectoryName);
			
			channelDirectory.ensureExists();
			
			return channelDirectory;
		}
		
	}
	
}
