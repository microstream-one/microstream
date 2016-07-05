package net.jadoth.storage.types;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.swizzling.types.SwizzleIdSet;

public interface StorageRequestTaskLoadByTids extends StorageRequestTaskLoad
{
	public final class Implementation extends StorageRequestTaskLoad.AbstractImplementation
	implements StorageRequestTaskLoadByTids, StorageChannelTaskLoadByOids
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final SwizzleIdSet tidList;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final long timestamp, final SwizzleIdSet tidList, final int channelCount)
		{
			super(timestamp, channelCount);
			this.tidList = tidList;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		protected final Binary internalProcessBy(final StorageChannel channel)
		{
			return channel.collectLoadByTids(this.tidList);
		}

	}

}
