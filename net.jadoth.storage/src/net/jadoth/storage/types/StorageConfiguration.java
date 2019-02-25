package net.jadoth.storage.types;

import static net.jadoth.X.mayNull;
import static net.jadoth.X.notNull;

import net.jadoth.chars.VarString;
import net.jadoth.typing.Immutable;

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
	
	public StorageFileProvider           backupFileProvider();

	
	public static StorageConfiguration New(
		final StorageChannelCountProvider   channelCountProvider  ,
		final StorageHousekeepingController housekeepingController,
		final StorageFileProvider           fileProvider          ,
		final StorageDataFileEvaluator      dataFileEvaluator     ,
		final StorageEntityCacheEvaluator   entityCacheEvaluator  ,
		final StorageFileProvider           backupFileProvider
	)
	{
		return new StorageConfiguration.Implementation(
			notNull(channelCountProvider)  ,
			notNull(housekeepingController),
			notNull(fileProvider)          ,
			notNull(dataFileEvaluator)     ,
			notNull(entityCacheEvaluator)  ,
			mayNull(backupFileProvider)
		);
	}

	public class Implementation implements StorageConfiguration, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageChannelCountProvider   channelCountProvider  ;
		private final StorageHousekeepingController housekeepingController;
		private final StorageFileProvider           fileProvider          ;
		private final StorageDataFileEvaluator      dataFileEvaluator     ;
		private final StorageEntityCacheEvaluator   entityCacheEvaluator  ;
		private final StorageFileProvider           backupFileProvider    ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final StorageChannelCountProvider   channelCountProvider  ,
			final StorageHousekeepingController housekeepingController,
			final StorageFileProvider           fileProvider          ,
			final StorageDataFileEvaluator      dataFileEvaluator     ,
			final StorageEntityCacheEvaluator   entityCacheEvaluator,
			final StorageFileProvider           backupFileProvider
		)
		{
			super();
			this.channelCountProvider   = channelCountProvider  ;
			this.housekeepingController = housekeepingController;
			this.entityCacheEvaluator   = entityCacheEvaluator  ;
			this.fileProvider           = fileProvider          ;
			this.dataFileEvaluator      = dataFileEvaluator     ;
			this.backupFileProvider     = backupFileProvider    ;
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
		public StorageFileProvider backupFileProvider()
		{
			return this.backupFileProvider;
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
		return new StorageConfiguration.Builder.Implementation<>();
	}
	
	public interface Builder<B extends Builder<?>>
	{
		public StorageChannelCountProvider channelCountProvider();
		
		public B setChannelCountProvider(StorageChannelCountProvider channelCountProvider);
		
		public StorageHousekeepingController housekeepingController();
		
		public B setHousekeepingController(StorageHousekeepingController housekeepingController);
		
		public StorageFileProvider storagefileProvider();
		
		public B setStorageFileProvider(StorageFileProvider storageFileProvider);
		
		public StorageFileProvider backupfileProvider();
		
		public B setBackupFileProvider(StorageFileProvider backupFileProvider);
		
		public StorageDataFileEvaluator fileEvaluator();
		
		public B setFileEvaluator(StorageDataFileEvaluator fileEvaluator);
		
		public StorageEntityCacheEvaluator entityCacheEvaluator();
		
		public B setEntityCacheEvaluator(StorageEntityCacheEvaluator entityCacheEvaluator);
		
		public StorageConfiguration createConfiguration();
		
		
		
		public class Implementation<B extends Builder.Implementation<?>> implements StorageConfiguration.Builder<B>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private StorageChannelCountProvider   channelCountProvider   = Storage.ChannelCountProvider()  ;
			private StorageHousekeepingController housekeepingController = Storage.HousekeepingController();
			private StorageFileProvider           storageFileProvider    = Storage.FileProvider()          ;
			private StorageDataFileEvaluator      fileEvaluator          = Storage.DataFileEvaluator()     ;
			private StorageEntityCacheEvaluator   entityCacheEvaluator   = Storage.EntityCacheEvaluator()  ;
			private StorageFileProvider           backupFileProvider    ; // optional
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Implementation()
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
			public StorageFileProvider backupfileProvider()
			{
				return this.backupFileProvider;
			}
			
			@Override
			public B setBackupFileProvider(final StorageFileProvider backupFileProvider)
			{
				// may be null
				this.backupFileProvider = backupFileProvider;
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
					this.backupFileProvider    ,
					this.fileEvaluator         ,
					this.entityCacheEvaluator  ,
					this.backupFileProvider
				);
			}
			
		}
		
	}

}
