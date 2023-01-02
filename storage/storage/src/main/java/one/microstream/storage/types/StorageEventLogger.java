package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import java.io.PrintWriter;
import java.io.StringWriter;
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
	 * @param channel the affected channel
	 * @param t the reason for the disruption
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
	
	
	/**
	 * Creates a NoOp StorageEventLogger that does really nothing.
	 * 
	 * @return a StorageEventLogger.NoOp instance
	 */
	public static StorageEventLogger NoOp()
	{
		return new StorageEventLogger.NoOp();
	}
	
	/**
	 * NoOp StorageEventLogger
	 * <p>
	 * Doesn't log any storage events
	 *
	 */
	public final class NoOp implements StorageEventLogger
	{
		NoOp()
		{
			super();
		}
	}
	
	
	/**
	 * Creates a Default StorageEventLogger thats prints to the console.
	 * 
	 * @return a StorageEventLogger.Default instance
	 */
	public static StorageEventLogger Default()
	{
		return new StorageEventLogger.Default(Default::printString);
	}
	
	/**
	 * Creates a Default StorageEventLogger that forwards its output to the supplied Consumer
	 * 
	 * @param messageConsumer a Consumer that processes the forwarded log messages
	 * @return a StorageEventLogger.Default instance
	 */
	public static StorageEventLogger Default(final Consumer<? super String> messageConsumer)
	{
		return new StorageEventLogger.Default(
			notNull(messageConsumer)
		);
	}
	
	/**
	 * Default implementation of StorageEventLogger
	 * <p>
	 * This implementation doesn't log behavior but logs exceptions
	 *
	 */
	public class Default implements StorageEventLogger
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
			return StorageChannel.class.getSimpleName()+ '#' + channelPart.channelIndex();
		}
		
		public static String stackTraceToString(final Throwable t)
		{
			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
			t.printStackTrace(printWriter);
			
			return printWriter.toString();
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		protected final Consumer<? super String> messageConsumer;
		
			
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final Consumer<? super String> messageConsumer)
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
		public void logDisruption(final StorageChannel channel, final Throwable t)
		{
			this.log(toChannelIdentifier(channel) + " encountered disrupting exception " + t);
			t.printStackTrace();
		}
	}
	
	
	/**
	 * Creates a Debug StorageEventLogger that prints to the console.
	 * 
	 * @return a StorageEventLogger.Debug instance
	 */
	public static StorageEventLogger Debug()
	{
		return new StorageEventLogger.Debug(Debug.Default::printString);
	}
	
	/**
	 * Creates a Debug StorageEventLogger forwards its output to the supplied Consumer
	 * 
	 * @param messageConsumer a Consumer that processes the forwarded log messages
	 * @return a StorageEventLogger.Debug instance
	 */
	public static StorageEventLogger Debug(final Consumer<? super String> messageConsumer)
	{
		return new StorageEventLogger.Debug(
			notNull(messageConsumer)
		);
	}
	
	/**
	 * Debug implementation of StorageEventLogger
	 * <p>
	 * This implementation logs behavior and exceptions
	 *
	 */
	public class Debug extends Default
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Debug(final Consumer<? super String> messageConsumer)
		{
			super(messageConsumer);
		}
		
				
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
			
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
