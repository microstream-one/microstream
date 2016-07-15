package net.jadoth.storage.types;

import net.jadoth.functional._longProcedure;

public interface StorageRootOidSelector extends _longProcedure
{
	public void reset();

	public long yield();



	public final class Implementation implements StorageRootOidSelector
	{
		private transient long currentMax;

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



		public final class Implementation implements StorageRootOidSelector.Provider
		{
			@Override
			public final StorageRootOidSelector provideRootOidSelector(final int channelIndex)
			{
				return new StorageRootOidSelector.Implementation();
			}

		}

	}

}
