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

import one.microstream.afs.types.AFS;
import one.microstream.afs.types.AFile;
import one.microstream.collections.types.XGettingEnum;


public interface StorageRequestTaskImportDataFiles extends StorageRequestTaskImportData<AFile>
{
	public final class Default
	extends    StorageRequestTaskImportData.Abstract<AFile>
	implements StorageRequestTaskImportDataFiles
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final long                          timestamp             ,
			final int                           channelCount          ,
			final StorageObjectIdRangeEvaluator objectIdRangeEvaluator,
			final XGettingEnum<AFile>           importFiles,
			final StorageOperationController    controller
		)
		{
			super(timestamp, channelCount, controller, objectIdRangeEvaluator, importFiles);
		}
		
		@Override
		protected StorageImportSource.Abstract createImportSource(
			final int                               channelIndex,
			final AFile                             file        ,
			final StorageChannelImportBatch.Default headBatch
		)
		{
			return new StorageImportSourceFile.Default(channelIndex, file, headBatch);
		}
		
		@Override
		protected void iterateSource(final AFile file, final ItemAcceptor itemAcceptor)
		{
			final StorageDataFileItemIterator iterator = StorageDataFileItemIterator.New(
				StorageDataFileItemIterator.BufferProvider.New(),
				itemAcceptor::accept
			);
			AFS.execute(file, iterator::iterateStoredItems);
		}

	}

}
