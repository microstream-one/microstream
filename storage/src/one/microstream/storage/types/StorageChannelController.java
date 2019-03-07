package one.microstream.storage.types;

import static one.microstream.X.notNull;

import one.microstream.collections.BulkList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.storage.exceptions.StorageExceptionDisruptingExceptions;


public interface StorageChannelController
{
	public StorageChannelCountProvider channelCountProvider();

	public boolean isChannelProcessingEnabled();
	
	public boolean checkProcessingEnabled() throws StorageExceptionDisruptingExceptions;
	
	public boolean registerDisruptingProblem(Throwable problem);
	
	public XGettingSequence<Throwable> disruptingProblems();
	
	public default boolean hasDisruptingProblems()
	{
		return !this.disruptingProblems().isEmpty();
	}
	
	public void setChannelProcessingEnabled(boolean enabled);

	public void activate();

	public void deactivate();



	public final class Implementation implements StorageChannelController
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final StorageChannelCountProvider channelCountProvider;
		private final BulkList<Throwable>         disruptingProblems = BulkList.New();
		private       boolean                     hasDisruptingProblems;
		
		private boolean channelProcessingEnabled;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final StorageChannelCountProvider channelCountProvider)
		{
			super();
			this.channelCountProvider = notNull(channelCountProvider);
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
				throw new StorageExceptionDisruptingExceptions(this.disruptingProblems.immure());
			}
			
			return this.channelProcessingEnabled;
		}

		@Override
		public final synchronized boolean registerDisruptingProblem(final Throwable problem)
		{
			this.disruptingProblems.add(problem);
			this.hasDisruptingProblems = true;
			this.channelProcessingEnabled = false;

			return this.disruptingProblems.size() == 1;
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
	
	
	public static StorageChannelController.Creator Provider()
	{
		return new StorageChannelController.Creator.Implementation();
	}
	
	public interface Creator
	{
		public StorageChannelController provideChannelController(
			StorageChannelCountProvider channelCountProvider
		);
		
		public final class Implementation implements StorageChannelController.Creator
		{
			Implementation()
			{
				super();
			}

			@Override
			public final StorageChannelController provideChannelController(
				final StorageChannelCountProvider channelCountProvider
			)
			{
				return new StorageChannelController.Implementation(
					notNull(channelCountProvider)
				);
			}
			
		}
		
	}

}
