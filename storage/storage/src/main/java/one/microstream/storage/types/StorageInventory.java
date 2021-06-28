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

import one.microstream.collections.types.XGettingTable;

public interface StorageInventory extends StorageHashChannelPart
{
	public XGettingTable<Long, StorageDataInventoryFile> dataFiles();

	public StorageTransactionsAnalysis transactionsFileAnalysis();


	
	public static StorageInventory New(
		final int                                           channelIndex        ,
		final XGettingTable<Long, StorageDataInventoryFile> dataFiles           ,
		final StorageTransactionsAnalysis                   transactionsAnalysis
	)
	{
		return new StorageInventory.Default(
			channelIndex        ,
			dataFiles           ,
			transactionsAnalysis
		);
	}

	public final class Default implements StorageInventory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final int                                           channelIndex        ;
		final XGettingTable<Long, StorageDataInventoryFile> dataFiles           ;
		final StorageTransactionsAnalysis                   transactionsAnalysis;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final int                                           channelIndex        ,
			final XGettingTable<Long, StorageDataInventoryFile> dataFiles           ,
			final StorageTransactionsAnalysis                   transactionsAnalysis
		)
		{
			super();
			this.channelIndex         = channelIndex        ;
			this.dataFiles            = dataFiles           ;
			this.transactionsAnalysis = transactionsAnalysis;
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
		public final XGettingTable<Long, StorageDataInventoryFile> dataFiles()
		{
			return this.dataFiles;
		}

		@Override
		public final StorageTransactionsAnalysis transactionsFileAnalysis()
		{
			return this.transactionsAnalysis;
		}

	}

}
