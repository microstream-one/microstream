package one.microstream.storage.types;

import one.microstream.persistence.binary.types.ChunksBuffer;
import one.microstream.storage.exceptions.StorageExceptionRequest;

public interface StorageRequestTaskLoad extends StorageRequestTask
{
	public ChunksBuffer result() throws StorageExceptionRequest;



	public abstract class Abstract extends StorageChannelTask.Abstract<ChunksBuffer>
	implements StorageRequestTaskLoad
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final ChunksBuffer[] result;
		


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final long timestamp, final int channelCount)
		{
			super(timestamp, channelCount);
			this.result = new ChunksBuffer[channelCount];
		}

		

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected final ChunksBuffer[] resultArray()
		{
			return this.result;
		}
		
		@Override
		protected void complete(final StorageChannel channel, final ChunksBuffer result) throws InterruptedException
		{
			this.result[channel.channelIndex()] = result;
			this.incrementCompletionProgress();
		}
		
		@Override
		public final ChunksBuffer result() throws StorageExceptionRequest
		{
			if(this.hasProblems())
			{
				throw new StorageExceptionRequest(this.problems());
			}
			
			// all channel result instances share the result array and there is always at least one channel
			return this.result[0];
		}

	}

}
