package net.jadoth.storage.types;

import java.nio.ByteBuffer;

import net.jadoth.memory.Chunks;
import net.jadoth.meta.JadothConsole;
import net.jadoth.util.JadothExceptions;
import net.jadoth.util.KeyValue;
import net.jadoth.util.chars.VarString;

public interface StorageRequestTaskSaveEntities extends StorageRequestTask
{
	public final class Implementation
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<KeyValue<ByteBuffer[], long[]>>
	implements StorageRequestTaskSaveEntities, StorageChannelTaskSaveEntities
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
		// override methods //
		/////////////////////

		@Override
		protected final KeyValue<ByteBuffer[], long[]> internalProcessBy(final StorageChannel channel)
		{
			this.DEBUG_Print(channel);
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
			.add(". EntityCounts: [")
			;
			for(final Chunks c : this.data)
			{
				vs.add(c.entityCount()).add(", ");
			}
			vs.deleteLast(2).add("]")
			;
			
			if(channel == null)
			{
				vs.add(" Stacktrace:");
				for(final StackTraceElement e : JadothExceptions.cutStacktraceByN(new Throwable(), 2).getStackTrace())
				{
					vs.lf().add(e.toString());
				}
			}

			JadothConsole.debugln(vs.toString(), 1);
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
