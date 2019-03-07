package net.jadoth.storage.types;

import net.jadoth.persistence.binary.types.ChunksBuffer;
import net.jadoth.persistence.types.PersistenceIdSet;

public interface StorageRequestTaskLoadByTids extends StorageRequestTaskLoad
{
	public final class Implementation extends StorageRequestTaskLoad.AbstractImplementation
	implements StorageRequestTaskLoadByTids, StorageChannelTaskLoadByOids
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceIdSet tidList;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final long timestamp, final PersistenceIdSet tidList, final int channelCount)
		{
			super(timestamp, channelCount);
			this.tidList = tidList;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final ChunksBuffer internalProcessBy(final StorageChannel channel)
		{
			return channel.collectLoadByTids(this.resultArray(), this.tidList);
		}

	}

}
