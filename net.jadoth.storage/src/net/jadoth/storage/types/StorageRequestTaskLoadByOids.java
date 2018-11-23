package net.jadoth.storage.types;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceIdSet;

public interface StorageRequestTaskLoadByOids extends StorageRequestTaskLoad
{
	public final class Implementation extends StorageRequestTaskLoad.AbstractImplementation
	implements StorageRequestTaskLoadByOids, StorageChannelTaskLoadByOids
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods    //
		/////////////////////

//		private static int calculateRequiredProgress(final SwizzleObjectIdSet[] data)
//		{
//			int requiredProgressCount = data.length;
//			for(int i = 0; i < data.length; i++)
//			{
//				if(data[i].isEmpty())
//				{
//					requiredProgressCount--;
//				}
//			}
//			return requiredProgressCount;
//		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceIdSet[] oidList;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final long timestamp, final PersistenceIdSet[] oidList)
		{
			/* (16.01.2014 TM)NOTE:
			 * using calculateRequiredProgress() here is a clear bug as a lower progress count (e.g. 1)
			 * does absolutely not guarantee that the processing channel(s) only have lower channel indices (e.g. 0)
			 * Absolutely astonishing that this worked correctly thousands of times in the last year and causes
			 * a problem just now.
			 */
			super(timestamp, oidList.length);
//			super(calculateRequiredProgress(oidList));
			this.oidList = oidList;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final Binary internalProcessBy(final StorageChannel channel)
		{
			return channel.collectLoadByOids(this.oidList[channel.channelIndex()]);
		}

	}

}
