package net.jadoth.storage.types;

import java.nio.ByteBuffer;

import net.jadoth.chars.VarString;
import net.jadoth.meta.XDebug;
import net.jadoth.persistence.binary.types.Chunks;
import net.jadoth.typing.KeyValue;
import net.jadoth.util.UtilStackTrace;

public interface StorageRequestTaskStoreEntities extends StorageRequestTask
{
	
	/* (11.08.2018 TM)TODO:
	 * The overly complex "KeyValue<ByteBuffer[], long[]>" construct could be replaced by a simple Long containing
	 * the channel's basePosition at which the chunk is stored as determined in StorageFileManager#storeChunks.
	 * Every sub-chunk's (ByteBuffer content's) file position could be calculated on the file by this while
	 * iterating them in the postCompletionSuccess logic.
	 * Preferable to a meaningless Long would be a "StorageChunkFilePosition" instance, containing a long value
	 * and, why not, a reference to the ByteBuffer[].
	 * The performance gain would probably not be noticeable, but it would simplify the source code.
	 * But for now (and while actually working on a network persistence demo and not the storage), the
	 * "never touch a running system" proverb applies.
	 */
	
	public final class Implementation
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<KeyValue<ByteBuffer[], long[]>>
	implements StorageRequestTaskStoreEntities, StorageChannelTaskStoreEntities
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final Chunks[] data;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(final long timestamp, final Chunks[] data)
		{
			// every channel has to store at least a chunk header, so progress count is always equal to channel count
			super(timestamp, data.length);
			this.data = data;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final KeyValue<ByteBuffer[], long[]> internalProcessBy(final StorageChannel channel)
		{
//			this.DEBUG_Print(channel);
			return channel.storeEntities(this.timestamp(), this.data[channel.channelIndex()]);
		}
		
		public final void DEBUG_Print(final StorageChannel channel)
		{
			final VarString vs = VarString.New();
			if(channel != null)
			{
				vs
				.add(channel.channelIndex())
				.add(" processing")
				;
			}
			else
			{
				vs
				.add("Issued")
				;
			}
			vs
			.add(" store task ")
			.add(System.identityHashCode(this))
			.add(" @")
			.add(this.timestamp())
			;
			
			if(channel == null)
			{
				vs.add(" Stacktrace:");
				for(final StackTraceElement e : UtilStackTrace.cutStacktraceByN(new Throwable(), 2).getStackTrace())
				{
					vs.lf().add(e.toString());
				}
			}

			XDebug.debugln(vs.toString(), 1);
		}

		@Override
		protected final void succeed(final StorageChannel channel, final KeyValue<ByteBuffer[], long[]> result)
		{
			// no storing operation of the other hash channels failed, so definitely commit the write here.
			channel.commitChunkStorage();
		}

		@Override
		protected final void postCompletionSuccess(
			final StorageChannel                 channel,
			final KeyValue<ByteBuffer[], long[]> result
		)
			throws InterruptedException
		{
			/* Post-completion logic that updates the storage channel's entity cache with the new entity data.
			 * this MIGHT come "too late" in terms of an entity that just got sweeped by GC but would now be
			 * referenced again.
			 * If such a case should pose a problem in an application (i.e. first releasing the last reference to an
			 * entity but at some later point wanting to reference it again without actually containing it in the data)
			 * has to be considered a business logic error that does not have to be covered by storage-level logic.
			 *
			 * If entity cache update should fail (which should never do)
			 * the problem has to (and can) be corrected (the necessary data has already been stored successfully).
			 * The task itself has already been reported as successful and the thread that issued
			 * and waited for the task already continued working.
			 */
			channel.postStoreUpdateEntityCache(result.key(), result.value());
		}

		@Override
		protected final void fail(final StorageChannel channel, final KeyValue<ByteBuffer[], long[]> result)
		{
			channel.rollbackChunkStorage();
		}

		@Override
		protected final void cleanUp(final StorageChannel channel)
		{
			// at this point the chunks are definitely not used anymore by anyone
			this.data[channel.channelIndex()].clear();

			// signal channel to cleanup the current store, e.g. remove pending store updates to re-enable GC sweeping
			channel.cleanupStore();
		}

	}

}
