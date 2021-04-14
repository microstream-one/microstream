package one.microstream.storage.types;

import java.lang.ref.WeakReference;

import one.microstream.collections.BulkList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.storage.exceptions.StorageExceptionDisruptingExceptions;


public interface StorageOperationController
{
	public StorageChannelCountProvider channelCountProvider();

	public boolean isChannelProcessingEnabled();
	
	public boolean checkProcessingEnabled() throws StorageExceptionDisruptingExceptions;
	
	public void registerDisruption(Throwable disruption);
	
	public XGettingSequence<Throwable> disruptions();
	
	public default boolean hasDisruptions()
	{
		return !this.disruptions().isEmpty();
	}
	
	public void setChannelProcessingEnabled(boolean enabled);

	public void activate();

	public void deactivate();


	
	public static StorageOperationController New(
		final StorageSystem               storageSystem       ,
		final StorageChannelCountProvider channelCountProvider
	)
	{
		return new StorageOperationController.Default(
			new WeakReference<>(storageSystem),
			channelCountProvider
		);
	}

	public final class Default implements StorageOperationController
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final WeakReference<StorageSystem> storageSystemReference;
		private final StorageChannelCountProvider  channelCountProvider  ;
		
		private final BulkList<Throwable> disruptions = BulkList.New();
		
		private boolean hasDisruptions;
		private boolean channelProcessingEnabled;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final WeakReference<StorageSystem> storageSystemReference,
			final StorageChannelCountProvider  channelCountProvider
		)
		{
			super();
			this.storageSystemReference = storageSystemReference;
			this.channelCountProvider   = channelCountProvider  ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final synchronized void setChannelProcessingEnabled(final boolean enabled)
		{
			this.channelProcessingEnabled = enabled;
		}

		@Override
		public final synchronized boolean isChannelProcessingEnabled()
		{
			// registering a problem sets this to false, so just checking this one field is enough.
			return this.channelProcessingEnabled;
		}

		@Override
		public final StorageChannelCountProvider channelCountProvider()
		{
			return this.channelCountProvider;
		}

		@Override
		public final synchronized void activate()
		{
			this.channelProcessingEnabled = true;
		}

		@Override
		public final synchronized void deactivate()
		{
			this.channelProcessingEnabled = false;
		}

		@Override
		public final synchronized boolean checkProcessingEnabled() throws StorageExceptionDisruptingExceptions
		{
			if(this.hasDisruptions)
			{
				// registering a problem has already set the processing flag to false.
				throw new StorageExceptionDisruptingExceptions(this.disruptions.immure());
			}
			
			// if the database managing instance is no longer reachable (used), there is no point in continue processing
			if(this.storageSystemReference.get() == null)
			{
//				XDebug.println(Thread.currentThread().getName() + " found nulled reference.");
				this.deactivate();
			}

//			XDebug.println(Thread.currentThread().getName() + " channelProcessingEnabled = " + this.channelProcessingEnabled);
			
			return this.channelProcessingEnabled;
		}

		@Override
		public final synchronized void registerDisruption(final Throwable disruption)
		{
			this.disruptions.add(disruption);
			this.hasDisruptions = true;
			this.channelProcessingEnabled = false;
		}

		@Override
		public final synchronized XGettingSequence<Throwable> disruptions()
		{
			return this.disruptions.immure();
		}
		
		@Override
		public final synchronized boolean hasDisruptions()
		{
			return this.hasDisruptions;
		}

	}
	
	
	public static StorageOperationController.Creator Provider()
	{
		return new StorageOperationController.Creator.Default();
	}
	
	public interface Creator
	{
		public StorageOperationController createOperationController(
			StorageChannelCountProvider channelCountProvider,
			StorageSystem               storageSystem
		);
		
		public final class Default implements StorageOperationController.Creator
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
			public final StorageOperationController createOperationController(
				final StorageChannelCountProvider channelCountProvider,
				final StorageSystem               storageSystem
			)
			{
				return StorageOperationController.New(storageSystem, channelCountProvider);
			}
			
		}
		
	}

}
