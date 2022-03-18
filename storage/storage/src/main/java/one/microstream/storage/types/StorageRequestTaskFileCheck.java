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


public interface StorageRequestTaskFileCheck extends StorageRequestTask
{
	public boolean result();



	public final class Default
	extends StorageChannelSynchronizingTask.AbstractCompletingTask<Void>
	implements StorageRequestTaskFileCheck, StorageChannelTaskStoreEntities
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final long    nanoTimeBudget;
		      boolean completed     ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final long                       timestamp     ,
			final int                        channelCount  ,
			final long                       nanoTimeBudget,
			final StorageOperationController controller
		)
		{
			super(timestamp, channelCount, controller);
			this.nanoTimeBudget = nanoTimeBudget;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final Void internalProcessBy(final StorageChannel channel)
		{
			this.completed = channel.issuedFileCleanupCheck(this.nanoTimeBudget);
			return null;
		}

		@Override
		public final boolean result()
		{
			return this.completed;
		}

	}

}
