package one.microstream.storage.types;

import one.microstream.persistence.binary.types.ChunksBuffer;

public interface StorageRequestTaskLoadRoots extends StorageRequestTaskLoad
{
	public final class Default extends StorageRequestTaskLoad.Abstract
	implements StorageRequestTaskLoadRoots
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final long timestamp, final int channelCount)
		{
			super(timestamp, channelCount);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final ChunksBuffer internalProcessBy(final StorageChannel channel)
		{
			// every channel returns the roots instances (in binary form) that he knows of, potentially none at all.
			return channel.collectLoadRoots(this.resultArray());
		}

	}

}
