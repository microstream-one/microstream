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

		protected Abstract(final long timestamp, final int channelCount, final StorageOperationController controller)
		{
			super(timestamp, channelCount, controller);
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
