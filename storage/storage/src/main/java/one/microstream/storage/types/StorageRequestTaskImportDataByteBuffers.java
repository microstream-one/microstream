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

import java.nio.ByteBuffer;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.BinaryEntityRawDataIterator;


public interface StorageRequestTaskImportDataByteBuffers extends StorageRequestTaskImportData<ByteBuffer>
{
	public final class Default
	extends    StorageRequestTaskImportData.Abstract<ByteBuffer>
	implements StorageRequestTaskImportDataByteBuffers
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final long                          timestamp             ,
			final int                           channelCount          ,
			final StorageObjectIdRangeEvaluator objectIdRangeEvaluator,
			final XGettingEnum<ByteBuffer>      importData,
			final StorageOperationController    controller
		)
		{
			super(timestamp, channelCount, controller, objectIdRangeEvaluator, importData);
		}
		
		@Override
		protected StorageImportSource.Abstract createImportSource(
			final int                               channelIndex,
			final ByteBuffer                        buffer      ,
			final StorageChannelImportBatch.Default headBatch
		)
		{
			return new StorageImportSourceByteBuffer.Default(channelIndex, buffer, headBatch);
		}
		
		@Override
		protected void iterateSource(final ByteBuffer buffer, final ItemAcceptor itemAcceptor)
		{
			final long address = XMemory.getDirectByteBufferAddress(buffer);
	    	BinaryEntityRawDataIterator.New().iterateEntityRawData(
	    		address,
	    		address + buffer.limit(),
	    		(entityStartAddress, dataBoundAddress) -> itemAcceptor.accept(
	    			entityStartAddress                   , // start is the same
	    			dataBoundAddress - entityStartAddress  // map to available item length
	    		)
	    	);
		}

	}

}
