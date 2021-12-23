package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import one.microstream.chars.VarString;
import one.microstream.typing.Immutable;

public interface StorageConfiguration
{
	public StorageChannelCountProvider channelCountProvider();

	public StorageHousekeepingController housekeepingController();

	public StorageEntityCacheEvaluator entityCacheEvaluator();

	/* (10.12.2014 TM)TODO: consolidate StorageConfiguration#fileProvider with FileWriter and FileReader
	 * either move both here as well or move fileProvider out of here.
	 */
	public StorageLiveFileProvider fileProvider();

	public StorageDataFileEvaluator dataFileEvaluator();
	
	public StorageBackupSetup backupSetup();

	
	/**
	 * Pseudo-constructor method to create a new {@link StorageConfiguration} instance
	 * using {@code null} as the {@link StorageBackupSetup} part and default instances for everything else.
	 * <p>
	 * For explanations and customizing values, see {@link StorageConfiguration.Builder}.
	 * 
	 * @return a new {@link StorageConfiguration} instance.
	 * 
	 * @see StorageConfiguration#New(StorageLiveFileProvider)
	 * @see StorageConfiguration.Builder
	 */
	public static StorageConfiguration New()
	{
		return StorageConfiguration.Builder()
			.createConfiguration()
		;
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageConfiguration} instance
	 * using the passed {@link StorageLiveFileProvider}, {@code null} as the {@link StorageBackupSetup} part
	 * and default instances for everything else.
	 * <p>
	 * For explanations and customizing values, see {@link StorageConfiguration.Builder}.
	 * 
	 * @param fileProvider the {@link StorageLiveFileProvider} to provide directory and file names.
	 * 
	 * @return a new {@link StorageConfiguration} instance.
	 * 
	 * @see StorageConfiguration#New()
	 * @see StorageConfiguration.Builder
	 */
	public static StorageConfiguration New(
		final StorageLiveFileProvider fileProvider
	)
	{
		return StorageConfiguration.Builder()
			.setStorageFileProvider(fileProvider)
			.createConfiguration()
		;
	}
	
	public static StorageConfiguration New(
		final StorageChannelCountProvider   channelCountProvider  ,
		final StorageHousekeepingController housekeepingController,
		final StorageLiveFileProvider           fileProvider          ,
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
		private final StorageLiveFileProvider           fileProvider          ;
		private final StorageDataFileEvaluator      dataFileEvaluator     ;
		private final StorageEntityCacheEvaluator   entityCacheEvaluator  ;
		private final StorageBackupSetup            backupSetup           ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final StorageChannelCountProvider   channelCountProvider  ,
			final StorageHousekeepingController housekeepingController,
			final StorageLiveFileProvider           fileProvider          ,
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
		public StorageLiveFileProvider fileProvider()
		{
			return this.fileProvider;
		}

		@Override
		public StorageDataFileEvaluator dataFileEvaluator()
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
				.add(this.backupSetup == null ? StorageBackupSetup.class.getName() + ": null": this.backupSetup).lf()
				.toString()
			;
		}

	}
	
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageConfiguration.Builder} instance.
	 * <p>
	 * For explanations and customizing values, see {@link StorageConfiguration.Builder}.
	 * 
	 * @return a new {@link StorageConfiguration.Builder} instance.
	 * 
	 * @see StorageConfiguration.Builder
	 * @see StorageConfiguration
	 */
	public static StorageConfiguration.Builder<?> Builder()
	{
		return new StorageConfiguration.Builder.Default<>();
	}
	
	public interface Builder<B extends Builder<?>>
	{
		public StorageChannelCountProvider channelCountProvider();
		
		public B setChannelCountProvider(StorageChannelCountProvider channelCountProvider);
		
		public StorageHousekeepingController housekeepingController();
		
		public B setHousekeepingController(StorageHousekeepingController housekeepingController);
		
		public StorageLiveFileProvider storagefileProvider();
		
		public B setStorageFileProvider(StorageLiveFileProvider liveFileProvider);
		
		public StorageBackupSetup backupSetup();
		
		public B setBackupSetup(StorageBackupSetup backupSetup);
		
		public StorageDataFileEvaluator dataFileEvaluator();
		
		public B setDataFileEvaluator(StorageDataFileEvaluator dataFileEvaluator);
		
		public StorageEntityCacheEvaluator entityCacheEvaluator();
		
		public B setEntityCacheEvaluator(StorageEntityCacheEvaluator entityCacheEvaluator);
		
		public StorageConfiguration createConfiguration();
		
		
		
		public class Default<B extends Builder.Default<?>> implements StorageConfiguration.Builder<B>
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private StorageChannelCountProvider   channelCountProvider   = this.initializeChannelCountProvider();
			private StorageHousekeepingController housekeepingController = this.initializeHousekeepingController();
			private StorageLiveFileProvider       storageFileProvider    = this.initializeLiveFileProvider();
			private StorageDataFileEvaluator      dataFileEvaluator      = this.initializeDataFileEvaluator();
			private StorageEntityCacheEvaluator   entityCacheEvaluator   = this.initializeEntityCacheEvaluator();
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
			
			protected StorageChannelCountProvider initializeChannelCountProvider()
			{
				return Storage.ChannelCountProvider();
			}
			
			protected StorageHousekeepingController initializeHousekeepingController()
			{
				return Storage.HousekeepingController();
			}
			
			protected StorageLiveFileProvider initializeLiveFileProvider()
			{
				return Storage.FileProvider();
			}
			
			protected StorageDataFileEvaluator initializeDataFileEvaluator()
			{
				return Storage.DataFileEvaluator();
			}
			
			protected StorageEntityCacheEvaluator initializeEntityCacheEvaluator()
			{
				return Storage.EntityCacheEvaluator();
			}
			
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
					? this.initializeChannelCountProvider()
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
					? this.initializeHousekeepingController()
					: housekeepingController
				;
				return this.$();
			}
			
			@Override
			public StorageLiveFileProvider storagefileProvider()
			{
				return this.storageFileProvider;
			}
			
			@Override
			public B setStorageFileProvider(final StorageLiveFileProvider liveFileProvider)
			{
				this.storageFileProvider = liveFileProvider == null
					? this.initializeLiveFileProvider()
					: liveFileProvider
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
			public StorageDataFileEvaluator dataFileEvaluator()
			{
				return this.dataFileEvaluator;
			}
			
			@Override
			public B setDataFileEvaluator(final StorageDataFileEvaluator dataFileEvaluator)
			{
				this.dataFileEvaluator = dataFileEvaluator == null
					? this.initializeDataFileEvaluator()
					: dataFileEvaluator
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
					? this.initializeEntityCacheEvaluator()
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
					this.dataFileEvaluator     ,
					this.entityCacheEvaluator  ,
					this.backupSetup
				);
			}
			
		}
		
	}

}
