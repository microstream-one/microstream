package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.meta.XDebug;

public interface StorageEventLogger
{
	public default void logChannelProcessingDisabled(final StorageChannel channel)
	{
		// no-op by default
	}
	
	public default void logChannelStoppedWorking(final StorageChannel channel)
	{
		// no-op by default
	}
	
	/**
	 * Note that not all Throwables are Exceptions. There are also Errors.
	 * And not all exceptions are problems. There are also program execution control vehicles like
	 * {@link InterruptedException}. The actually fitting common term is "Disruption".
	 * Throwable is a very low-level technical, compiler-oriented expression.
	 * 
	 * @param channel
	 * @param t
	 */
	public default void logDisruption(final StorageChannel channel, final Throwable t)
	{
		// no-op by default
	}
	
	public default void logLiveCheckComplete(final StorageEntityCache<?> entityCache)
	{
		// no-op by default
	}
	
	

	public default void logGarbageCollectorSweepingComplete(final StorageEntityCache<?> entityCache)
	{
		// no-op by default
	}
	
	public default void logGarbageCollectorNotNeeded()
	{
		// no-op by default
	}
	
	public default void logGarbageCollectorCompletedHotPhase(final long gcHotGeneration, final long lastGcHotCompletion)
	{
		// no-op by default
	}
	
	public default void logGarbageCollectorCompleted(final long gcColdGeneration, final long lastGcColdCompletion)
	{
		// no-op by default
	}

	public default void logGarbageCollectorEncounteredZombieObjectId(final long objectId)
	{
		// no-op by default
	}
	
	
	
	public static StorageEventLogger NoOp()
	{
		return new StorageEventLogger.NoOp();
	}
	
	public final class NoOp implements StorageEventLogger
	{
		NoOp()
		{
			super();
		}
	}
	
	
	
	public static StorageEventLogger Debug()
	{
		return new StorageEventLogger.Debug(Debug::printString);
	}
	
	public static StorageEventLogger Debug(final Consumer<? super String> messageConsumer)
	{
		return new StorageEventLogger.Debug(
			notNull(messageConsumer)
		);
	}
	
	public class Debug implements StorageEventLogger
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static void printString(final String s)
		{
			XDebug.println(s, 4);
		}
		
		public static String toChannelIdentifier(final StorageChannel channel)
		{
			return toChannelPartIdentifier(channel);
		}
		
		public static String toChannelIdentifier(final StorageEntityCache<?> entityCache)
		{
			return toChannelPartIdentifier(entityCache);
		}
		
		public static String toChannelPartIdentifier(final StorageHashChannelPart channelPart)
		{
			return "StorageChannel#" + channelPart.channelIndex();
		}
		
		
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Consumer<? super String> messageConsumer;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Debug(final Consumer<? super String> messageConsumer)
		{
			super();
			this.messageConsumer = notNull(messageConsumer);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public void log(final String s)
		{
			this.messageConsumer.accept(s);
		}
		
		@Override
		public void logChannelProcessingDisabled(final StorageChannel channel)
		{
			this.log(toChannelIdentifier(channel) + " processing disabled.");
		}
		
		@Override
		public void logChannelStoppedWorking(final StorageChannel channel)
		{
			this.log(toChannelIdentifier(channel) + " stopped working.");
		}
		
		@Override
		public void logDisruption(final StorageChannel channel, final Throwable t)
		{
			this.log(toChannelIdentifier(channel) + " encountered exception " + t);
		}
		
		@Override
		public void logLiveCheckComplete(final StorageEntityCache<?> entityCache)
		{
			this.log(toChannelIdentifier(entityCache) + " completed live check.");
		}
		
		@Override
		public void logGarbageCollectorSweepingComplete(final StorageEntityCache<?> entityCache)
		{
			this.log(toChannelIdentifier(entityCache) + " completed sweeping.");
		}
		
		@Override
		public void logGarbageCollectorEncounteredZombieObjectId(final long objectId)
		{
			this.log("GC marking encountered zombie ObjectId " + objectId);
		}
		
		@Override
		public void logGarbageCollectorNotNeeded()
		{
			this.log("not needed.");
		}
		
		@Override
		public void logGarbageCollectorCompletedHotPhase(final long gcHotGeneration, final long lastGcHotCompletion)
		{
			this.log("Completed GC Hot Phase #" + gcHotGeneration + " @ " + lastGcHotCompletion);
		}
		
		@Override
		public void logGarbageCollectorCompleted(final long gcColdGeneration, final long lastGcColdCompletion)
		{
			this.log("Storage-GC completed #" + gcColdGeneration + " @ " + lastGcColdCompletion);
		}
		
	}
	
}
