package one.microstream.storage.types;

import one.microstream.functional._longProcedure;

public interface StorageRootOidSelector extends _longProcedure
{
	public void reset();

	public long yield();

	public default void resetGlobal()
	{
		this.reset();
	}

	public default void acceptGlobal(final long rootOid)
	{
		this.accept(rootOid);
	}

	public default long yieldGlobal()
	{
		return this.yield();
	}



	public final class Default implements StorageRootOidSelector
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private transient long currentMax;
		
		
		
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
		public final void accept(final long rootOid)
		{
			if(rootOid < this.currentMax)
			{
				return;
			}
			this.currentMax = rootOid;
		}

		@Override
		public final void reset()
		{
			this.currentMax = 0;

		}

		@Override
		public final long yield()
		{
			return this.currentMax;
		}

	}



	public interface Provider
	{
		public StorageRootOidSelector provideRootOidSelector(int channelIndex);



		public final class Default implements StorageRootOidSelector.Provider
		{
			@Override
			public final StorageRootOidSelector provideRootOidSelector(final int channelIndex)
			{
				return new StorageRootOidSelector.Default();
			}

		}

	}

}
