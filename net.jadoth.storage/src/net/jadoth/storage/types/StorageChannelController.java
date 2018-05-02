package net.jadoth.storage.types;

import static net.jadoth.Jadoth.notNull;


public interface StorageChannelController
{
	public StorageChannelCountProvider channelCountProvider();

	public boolean isChannelProcessingEnabled();

	public void activate();

	public void deactivate();



	public final class Implementation implements StorageChannelController
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final StorageChannelCountProvider channelCountProvider;

		private volatile boolean channelProcessingEnabled;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final StorageChannelCountProvider channelCountProvider)
		{
			super();
			this.channelCountProvider = notNull(channelCountProvider);
		}



		///////////////////////////////////////////////////////////////////////////
		// setters          //
		/////////////////////

		public void setChannelProcessingEnabled(final boolean enabled)
		{
			this.channelProcessingEnabled = enabled;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final boolean isChannelProcessingEnabled()
		{
			return this.channelProcessingEnabled;
		}

		@Override
		public final StorageChannelCountProvider channelCountProvider()
		{
			return this.channelCountProvider;
		}

		@Override
		public final void activate()
		{
			this.channelProcessingEnabled = true;
		}

		@Override
		public final void deactivate()
		{
			this.channelProcessingEnabled = false;
		}

	}

}
