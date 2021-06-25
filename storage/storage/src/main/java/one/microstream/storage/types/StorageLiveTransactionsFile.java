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
import static one.microstream.math.XMath.notNegative;

import one.microstream.afs.types.AFile;
import one.microstream.storage.types.StorageTransactionsAnalysis.EntryIterator;

public interface StorageLiveTransactionsFile
extends StorageTransactionsFile, StorageLiveChannelFile<StorageLiveTransactionsFile>
{
	@Override
	public default StorageBackupTransactionsFile ensureBackupFile(final StorageBackupInventory backupInventory)
	{
		return backupInventory.ensureTransactionsFile(this);
	}
	
	
	public <P extends EntryIterator> P processBy(P iterator);
	
	
	public static StorageLiveTransactionsFile New(
		final AFile file        ,
		final int   channelIndex
	)
	{
		return new StorageLiveTransactionsFile.Default(
			    notNull(file),
			notNegative(channelIndex)
		);
	}
	
	
	
	
	public final class Default
	extends StorageLiveFile.Abstract<StorageLiveTransactionsFile>
	implements StorageLiveTransactionsFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int channelIndex;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final AFile file, final int channelIndex)
		{
			super(file);
			this.channelIndex = channelIndex;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final int channelIndex()
		{
			return this.channelIndex;
		}
		
		@Override
		public <P extends EntryIterator> P processBy(final P iterator)
		{
			StorageTransactionsAnalysis.Logic.processInputFile(
				this.ensureReadable(),
				iterator
			);
			
			return iterator;
		}
		
	}
	
}
