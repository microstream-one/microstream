package one.microstream.storage.exceptions;

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

import one.microstream.collections.types.XGettingTable;
import one.microstream.storage.types.StorageBackupDataFile;
import one.microstream.storage.types.StorageInventory;

public class StorageExceptionBackupEmptyStorageBackupAhead
extends StorageExceptionBackupChannelIndex
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final StorageInventory                          storageInventory;
	private final XGettingTable<Long, StorageBackupDataFile> backupFiles     ;

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionBackupEmptyStorageBackupAhead(
		final StorageInventory                           storageInventory,
		final XGettingTable<Long, StorageBackupDataFile> backupFiles
	)
	{
		super(storageInventory.channelIndex());
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
	}

	public StorageExceptionBackupEmptyStorageBackupAhead(
		final StorageInventory                           storageInventory,
		final XGettingTable<Long, StorageBackupDataFile> backupFiles     ,
		final String                                     message
	)
	{
		super(storageInventory.channelIndex(), message);
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
	}

	public StorageExceptionBackupEmptyStorageBackupAhead(
		final StorageInventory                           storageInventory,
		final XGettingTable<Long, StorageBackupDataFile> backupFiles     ,
		final Throwable                                  cause
	)
	{
		super(storageInventory.channelIndex(), cause);
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
	}

	public StorageExceptionBackupEmptyStorageBackupAhead(
		final StorageInventory                           storageInventory,
		final XGettingTable<Long, StorageBackupDataFile> backupFiles     ,
		final String                                     message         ,
		final Throwable                                  cause
	)
	{
		super(storageInventory.channelIndex(), message, cause);
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
	}

	public StorageExceptionBackupEmptyStorageBackupAhead(
		final StorageInventory                           storageInventory  ,
		final XGettingTable<Long, StorageBackupDataFile> backupFiles       ,
		final String                                     message           ,
		final Throwable                                  cause             ,
		final boolean                                    enableSuppression ,
		final boolean                                    writableStackTrace
	)
	{
		super(storageInventory.channelIndex(), message, cause, enableSuppression, writableStackTrace);
		this.storageInventory = storageInventory;
		this.backupFiles      = backupFiles     ;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final StorageInventory storageInventory()
	{
		return this.storageInventory;
	}
	
	public final XGettingTable<Long, StorageBackupDataFile> backupFiles()
	{
		return this.backupFiles;
	}
	
}
