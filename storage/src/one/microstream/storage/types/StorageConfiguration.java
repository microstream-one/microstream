package one.microstream.storage.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import one.microstream.chars.VarString;
import one.microstream.typing.Immutable;

public interface StorageConfiguration
{
	public StorageChannelCountProvider   channelCountProvider();

	public StorageHousekeepingController housekeepingController();

	public StorageEntityCacheEvaluator   entityCacheEvaluator();

	/* (10.12.2014 TM)TODO: consolidate StorageConfiguration#fileProvider with FileWriter and FileReader
	 * either move both here as well or move fileProvider out of here.
	 */
	public StorageFileProvider           fileProvider();

	public StorageDataFileEvaluator      fileEvaluator();
	
	public StorageBackupSetup           backupSetup();

	
	public static StorageConfiguration New(
		final StorageChannelCountProvider   channelCountProvider  ,
		final StorageHousekeepingController housekeepingController,
		final StorageFileProvider           fileProvider          ,
		final StorageDataFileEvaluator      dataFileEvaluator     ,
		final StorageEntityCacheEvaluator   entityCacheEvaluator  ,
		final StorageBackupSetup            backupSetup
	)
	{
		return new StorageConfiguration.Default(
			notNull(channelCountProvider)  ,
			notNull(housekeepingController),
			notNull(fileProvider)          ,
			notNull(dataFileEvaluator)     ,
			notNull(entityCacheEvaluator)  ,
			mayNull(backupSetup)
		);
	}

	public class Default implements StorageConfiguration, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageChannelCountProvider   channelCountProvider  ;
		private final StorageHousekeepingController housekeepingController;
		private final StorageFileProvider           fileProvider          ;
		private final StorageDataFileEvaluator      dataFileEvaluator     ;
		private final StorageEntityCacheEvaluator   entityCacheEvaluator  ;
		private final StorageBackupSetup            backupSetup           ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final StorageChannelCountProvider   channelCountProvider  ,
			final StorageHousekeepingController housekeepingController,
			final StorageFileProvider           fileProvider          ,
			final StorageDataFileEvaluator      dataFileEvaluator     ,
			final StorageEntityCacheEvaluator   entityCacheEvaluator  ,
			final StorageBackupSetup            backupSetup
		)
		{
			super();
			this.channelCountProvider   = channelCountProvider  ;
			this.housekeepingController = housekeepingController;
			this.entityCacheEvaluator   = entityCacheEvaluator  ;
			this.fileProvider           = fileProvider          ;
			this.dataFileEvaluator      = dataFileEvaluator     ;
			this.backupSetup            = backupSetup           ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public StorageChannelCountProvider channelCountProvider()
		{
			return this.channelCountProvider;
		}

		@Override
		public StorageHousekeepingController housekeepingController()
		{
			return this.housekeepingController;
		}

		@Override
		public StorageEntityCacheEvaluator entityCacheEvaluator()
		{
			return this.entityCacheEvaluator;
		}

		@Override
		public StorageFileProvider fileProvider()
		{
			return this.fileProvider;
		}

		@Override
		public StorageDataFileEvaluator fileEvaluator()
		{
			return this.dataFileEvaluator;
		}
		
		@Override
		public StorageBackupSetup backupSetup()
		{
			return this.backupSetup;
		}

		@Override
		public String toString()
		{
			return VarString.New()
				.add(this.getClass().getName()  ).add(':').lf()
				.add(this.channelCountProvider  ).lf()
				.add(this.fileProvider          ).lf()
				.add(this.housekeepingController).lf()
				.add(this.entityCacheEvaluator  ).lf()
				.add(this.dataFileEvaluator     ).lf()
				.toString()
			;
		}

	}
	
	
	
	public static Builder<?> Builder()
	{
		return new StorageConfiguration.Builder.Default<>();
	}
	
	public interface Builder<B extends Builder<?>>
	{
		public StorageChannelCountProvider channelCountProvider();
		
		public B setChannelCountProvider(StorageChannelCountProvider channelCountProvider);
		
		public StorageHousekeepingController housekeepingController();
		
		public B setHousekeepingController(StorageHousekeepingController housekeepingController);
		
		public StorageFileProvider storagefileProvider();
		
		public B setStorageFileProvider(StorageFileProvider storageFileProvider);
		
		public StorageBackupSetup backupSetup();
		
		public B setBackupSetup(StorageBackupSetup backupSetup);
		
		public StorageDataFileEvaluator fileEvaluator();
		
		public B setFileEvaluator(StorageDataFileEvaluator fileEvaluator);
		
		public StorageEntityCacheEvaluator entityCacheEvaluator();
		
		public B setEntityCacheEvaluator(StorageEntityCacheEvaluator entityCacheEvaluator);
		
		public StorageConfiguration createConfiguration();
		
		
		
		public class Default<B extends Builder.Default<?>> implements StorageConfiguration.Builder<B>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private StorageChannelCountProvider   channelCountProvider   = Storage.ChannelCountProvider()  ;
			private StorageHousekeepingController housekeepingController = Storage.HousekeepingController();
			private StorageFileProvider           storageFileProvider    = Storage.FileProvider()          ;
			private StorageDataFileEvaluator      fileEvaluator          = Storage.DataFileEvaluator()     ;
			private StorageEntityCacheEvaluator   entityCacheEvaluator   = Storage.EntityCacheEvaluator()  ;
			private StorageBackupSetup            backupSetup           ; // optional
			
			
			
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
			
			@SuppressWarnings("unchecked")
			protected final B $()
			{
				return (B)this;
			}
			
			@Override
			public StorageChannelCountProvider channelCountProvider()
			{
				return this.channelCountProvider;
			}
			
			@Override
			public B setChannelCountProvider(final StorageChannelCountProvider channelCountProvider)
			{
				this.channelCountProvider = channelCountProvider == null
					? Storage.ChannelCountProvider()
					: channelCountProvider
				;
				return this.$();
			}
			
			@Override
			public StorageHousekeepingController housekeepingController()
			{
				return this.housekeepingController;
			}
			
			@Override
			public B setHousekeepingController(final StorageHousekeepingController housekeepingController)
			{
				this.housekeepingController = housekeepingController == null
					? Storage.HousekeepingController()
					: housekeepingController
				;
				return this.$();
			}
			
			@Override
			public StorageFileProvider storagefileProvider()
			{
				return this.storageFileProvider;
			}
			
			@Override
			public B setStorageFileProvider(final StorageFileProvider storageFileProvider)
			{
				this.storageFileProvider = storageFileProvider == null
					? Storage.FileProvider()
					: storageFileProvider
				;
				return this.$();
			}
			
			@Override
			public StorageBackupSetup backupSetup()
			{
				return this.backupSetup;
			}
			
			@Override
			public B setBackupSetup(final StorageBackupSetup backupSetup)
			{
				// may be null
				this.backupSetup = backupSetup;
				return this.$();
			}
			
			@Override
			public StorageDataFileEvaluator fileEvaluator()
			{
				return this.fileEvaluator;
			}
			
			@Override
			public B setFileEvaluator(final StorageDataFileEvaluator fileEvaluator)
			{
				this.fileEvaluator = fileEvaluator == null
					? Storage.DataFileEvaluator()
					: fileEvaluator
				;
				return this.$();
			}
			
			@Override
			public StorageEntityCacheEvaluator entityCacheEvaluator()
			{
				return this.entityCacheEvaluator;
			}
			
			@Override
			public B setEntityCacheEvaluator(final StorageEntityCacheEvaluator entityCacheEvaluator)
			{
				this.entityCacheEvaluator = entityCacheEvaluator == null
					? Storage.EntityCacheEvaluator()
					: entityCacheEvaluator
				;
				return this.$();
			}
			
			@Override
			public StorageConfiguration createConfiguration()
			{
				return StorageConfiguration.New(
					this.channelCountProvider  ,
					this.housekeepingController,
					this.storageFileProvider   ,
					this.fileEvaluator         ,
					this.entityCacheEvaluator  ,
					this.backupSetup
				);
			}
			
		}
		
	}

}
