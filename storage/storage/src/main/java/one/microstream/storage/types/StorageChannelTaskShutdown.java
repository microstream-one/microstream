package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

public interface StorageChannelTaskShutdown extends StorageChannelTask
{
	public final class Default
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<Void>
	implements StorageChannelTaskShutdown
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageOperationController operationController;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final long                       timestamp          ,
			final int                        channelCount       ,
			final StorageOperationController operationController
		)
		{
			super(timestamp, channelCount, operationController);
			this.operationController = notNull(operationController);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final Void internalProcessBy(final StorageChannel channel)
		{
			// may not deactivate here as some channel threads would die before all others notice the progress
			return null;
		}

		@Override
		protected final void succeed(final StorageChannel channel, final Void result)
		{
			/* (01.07.2015 TM)FIXME: Shutdown lets "remainingForCompletion" remain at full channel count,
			 * thus letting the calling thread (main) wait forever
			 * thus preventing the program from terminating.
			 */
			/* (07.07.2016 TM)FIXME: Shutdown must properly handle completion notification
			 * so that the issuing shutdown method waits for the shutdown to actually complete.
			 */

			// can / may never throw an exception
			channel.reset();
		}

		@Override
		protected final void fail(final StorageChannel channel, final Void result)
		{
			// nothing to do here
		}

	}

}
