package one.microstream.storage.types;

import one.microstream.persistence.binary.types.ChunksBuffer;
import one.microstream.persistence.types.PersistenceIdSet;

public interface StorageRequestTaskLoadByTids extends StorageRequestTaskLoad
{
	public final class Default extends StorageRequestTaskLoad.Abstract
	implements StorageRequestTaskLoadByTids, StorageChannelTaskLoadByOids
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceIdSet tidList;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final long timestamp, final PersistenceIdSet tidList, final int channelCount)
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
