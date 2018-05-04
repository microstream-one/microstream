package net.jadoth.storage.types;

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



	public class Implementation implements StorageConfiguration, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageChannelCountProvider   channelCountProvider  ;
		private final StorageHousekeepingController housekeepingController;
		private final StorageFileProvider           fileProvider          ;
		private final StorageDataFileEvaluator      fileEvaluator         ;
		private final StorageEntityCacheEvaluator   entityCacheEvaluator  ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(
			final StorageChannelCountProvider   channelCountProvider  ,
			final StorageHousekeepingController housekeepingController,
			final StorageFileProvider           fileProvider          ,
			final StorageDataFileEvaluator      fileEvaluator         ,
			final StorageEntityCacheEvaluator   entityCacheEvaluator
		)
		{
			super();
			this.channelCountProvider   = notNull(channelCountProvider)  ;
			this.housekeepingController = notNull(housekeepingController);
			this.entityCacheEvaluator   = notNull(entityCacheEvaluator)  ;
			this.fileProvider           = notNull(fileProvider)          ;
			this.fileEvaluator          = notNull(fileEvaluator)         ;
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
			return this.fileEvaluator;
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
				.add(this.fileEvaluator         ).lf()
				.toString()
			;
		}

	}

}
