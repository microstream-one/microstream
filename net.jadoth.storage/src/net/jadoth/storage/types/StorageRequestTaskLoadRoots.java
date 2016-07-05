package net.jadoth.storage.types;

import net.jadoth.persistence.binary.types.Binary;

public interface StorageRequestTaskLoadRoots extends StorageRequestTaskLoad
{
	public final class Implementation extends StorageRequestTaskLoad.AbstractImplementation
	implements StorageRequestTaskLoadRoots
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final long timestamp, final int channelCount)
		{
			super(timestamp, channelCount);
		}




		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		protected final Binary internalProcessBy(final StorageChannel channel)
		{
			// every channel returns the roots instances (in binary form) that he knows of, potentially none at all.
			return channel.collectLoadRoots();
		}

	}

}
