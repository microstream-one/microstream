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

import one.microstream.persistence.binary.types.ChunksBuffer;
import one.microstream.persistence.types.PersistenceIdSet;

public interface StorageRequestTaskLoadByOids extends StorageRequestTaskLoad
{
	public final class Default extends StorageRequestTaskLoad.Abstract
	implements StorageRequestTaskLoadByOids, StorageChannelTaskLoadByOids
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceIdSet[] oidList;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final long timestamp, final PersistenceIdSet[] oidList, final StorageOperationController controller)
		{
			/* (16.01.2014 TM)NOTE:
			 * using calculateRequiredProgress() here is a clear bug as a lower progress count (e.g. 1)
			 * does absolutely not guarantee that the processing channel(s) only have lower channel indices (e.g. 0)
			 * Absolutely astonishing that this worked correctly thousands of times in the last year and causes
			 * a problem just now.
			 */
			super(timestamp, oidList.length, controller);
//			super(calculateRequiredProgress(oidList));
			this.oidList = oidList;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		protected final ChunksBuffer internalProcessBy(final StorageChannel channel)
		{
			return channel.collectLoadByOids(this.resultArray(), this.oidList[channel.channelIndex()]);
		}

	}

}
