package net.jadoth.storage.types;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.storage.exceptions.StorageExceptionRequest;

public interface StorageRequestTaskLoad extends StorageRequestTask
{
	public Binary[] result() throws StorageExceptionRequest;



	public abstract class AbstractImplementation extends StorageChannelTask.AbstractImplementation<Binary>
	implements StorageRequestTaskLoad
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final Binary[] result;
		


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractImplementation(final long timestamp, final int hashRange)
		{
			super(timestamp, hashRange);
			this.result = new Binary[hashRange];
		}

		

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		protected void complete(final StorageChannel channel, final Binary result) throws InterruptedException
		{
			this.result[channel.channelIndex()] = result;
			this.incrementCompletionProgress();
		}
		
		@Override
		public final Binary[] result() throws StorageExceptionRequest
		{
			if(this.hasProblems())
			{
				throw new StorageExceptionRequest(this.problems());
			}
			return this.result;
		}

	}

}
