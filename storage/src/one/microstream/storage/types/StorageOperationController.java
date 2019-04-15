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
	
	public void registerDisruptingProblem(Throwable problem);
	
	public XGettingSequence<Throwable> disruptingProblems();
	
	public default boolean hasDisruptingProblems()
	{
		return !this.disruptingProblems().isEmpty();
	}
	
	public void setChannelProcessingEnabled(boolean enabled);

	public void activate();

	public void deactivate();


	
	public static StorageOperationController New(
		final StorageManager              storageManager      ,
		final StorageChannelCountProvider channelCountProvider
	)
	{
		return new StorageOperationController.Implementation(
			new WeakReference<>(storageManager),
			channelCountProvider
		);
	}

	public final class Implementation implements StorageOperationController
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final WeakReference<StorageManager> storageManagerReference;
		private final StorageChannelCountProvider   channelCountProvider   ;
		private final BulkList<Throwable>           disruptingProblems      = BulkList.New();
		
		private boolean hasDisruptingProblems   ;
		private boolean channelProcessingEnabled;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final WeakReference<StorageManager> storageManagerReference,
			final StorageChannelCountProvider   channelCountProvider
		)
		{
			super();
			this.storageManagerReference = storageManagerReference;
			this.channelCountProvider    = channelCountProvider   ;
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
			
			if(this.hasDisruptingProblems)
			{
				// registering a problem has already set the processing flag to false.
				throw new StorageExceptionDisruptingExceptions(this.disruptingProblems.immure());
			}
			
			// if the database managing instance is no longer reachable (used), there is no point in continue processing
			if(this.storageManagerReference.get() == null)
			{
//				XDebug.println(Thread.currentThread().getName() + " found nulled reference.");
				this.deactivate();
			}

//			XDebug.println(Thread.currentThread().getName() + " channelProcessingEnabled = " + this.channelProcessingEnabled);
			
			return this.channelProcessingEnabled;
		}

		@Override
		public final synchronized void registerDisruptingProblem(final Throwable problem)
		{
			this.disruptingProblems.add(problem);
			this.hasDisruptingProblems = true;
			this.channelProcessingEnabled = false;
		}

		@Override
		public final synchronized XGettingSequence<Throwable> disruptingProblems()
		{
			return this.disruptingProblems.immure();
		}
		
		@Override
		public final synchronized boolean hasDisruptingProblems()
		{
			return this.hasDisruptingProblems;
		}

	}
	
	
	public static StorageOperationController.Creator Provider()
	{
		return new StorageOperationController.Creator.Implementation();
	}
	
	public interface Creator
	{
		public StorageOperationController createOperationController(
			StorageChannelCountProvider channelCountProvider,
			StorageManager              storageManager
		);
		
		public final class Implementation implements StorageOperationController.Creator
		{
			Implementation()
			{
				super();
			}

			@Override
			public final StorageOperationController createOperationController(
				final StorageChannelCountProvider channelCountProvider,
				final StorageManager              storageManager
			)
			{
				return StorageOperationController.New(
					storageManager      ,
					channelCountProvider
				);
			}
			
		}
		
	}

}
