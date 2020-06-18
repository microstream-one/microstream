package one.microstream.storage.types;

import static one.microstream.X.notNull;

import one.microstream.afs.ADirectory;

public interface StorageDirectoryStructureProvider
{
	public ADirectory provideChannelDirectory(ADirectory storageRootDirectory, int channelIndex);
	
	
	
	public static StorageDirectoryStructureProvider New()
	{
		return New(StorageFileProvider.Defaults.defaultChannelDirectoryPrefix());
	}
	
	public static StorageDirectoryStructureProvider New(final String channelDirectoryPrefix)
	{
		return new StorageDirectoryStructureProvider.Default(
			notNull(channelDirectoryPrefix)
		);
	}
	
	public final class Default implements StorageDirectoryStructureProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String channelDirectoryPrefix;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final String channelDirectoryPrefix)
		{
			super();
			this.channelDirectoryPrefix = channelDirectoryPrefix;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public final String channelDirectoryPrefix()
		{
			return this.channelDirectoryPrefix;
		}

		@Override
		public final ADirectory provideChannelDirectory(
			final ADirectory storageRootDirectory,
			final int        hashIndex
		)
		{
			final String channelDirectoryName = this.channelDirectoryPrefix() + hashIndex;
			final ADirectory channelDirectory = storageRootDirectory.ensureDirectory(channelDirectoryName);
			
			channelDirectory.ensureExists();
			
			return channelDirectory;
		}
	}
}
