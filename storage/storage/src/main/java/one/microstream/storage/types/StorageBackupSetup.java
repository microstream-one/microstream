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

import one.microstream.afs.types.ADirectory;
import one.microstream.storage.types.StorageDataFileValidator.Creator;

public interface StorageBackupSetup
{
	public StorageBackupFileProvider backupFileProvider();
	
	public StorageFileWriter.Provider setupWriterProvider(
		StorageFileWriter.Provider writerProvider
	);
	
	public StorageBackupHandler setupHandler(
		StorageOperationController       operationController,
		StorageWriteController           writeController    ,
		StorageDataFileValidator.Creator backupDataFileValidatorCreator
	);
	

	
	/**
	 * Pseudo-constructor method to create a new {@link StorageBackupSetup} instance
	 * using the passed directory as the backup location.
	 * <p>
	 * For explanations and customizing values, see {@link StorageBackupSetup#New(StorageBackupFileProvider)}.
	 * 
	 * @param backupDirectory the directory where the backup shall be located.
	 * 
	 * @return a new {@link StorageBackupSetup} instance.
	 * 
	 * @see StorageBackupSetup#New(StorageBackupFileProvider)
	 * @see StorageBackupHandler
	 */
	
	public static StorageBackupSetup New(final ADirectory backupDirectory)
	{
		final StorageBackupFileProvider backupFileProvider = StorageBackupFileProvider.Builder(
			backupDirectory.fileSystem()
		)
			.setDirectory(backupDirectory)
			.createFileProvider()
		;
		return New(backupFileProvider);
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link StorageBackupSetup} instance
	 * using the passed {@link StorageLiveFileProvider}.
	 * <p>
	 * A StorageBackupSetup basically defines where the backup files will be located by the {@link StorageBackupHandler}.
	 * 
	 * @param backupFileProvider the {@link StorageBackupFileProvider} to define where the backup files will be located.
	 * 
	 * @return a new {@link StorageBackupSetup} instance.
	 * 
	 * @see StorageBackupSetup#New(ADirectory)
	 * @see StorageBackupHandler
	 */
	public static StorageBackupSetup New(final StorageBackupFileProvider backupFileProvider)
	{
		return new StorageBackupSetup.Default(
			notNull(backupFileProvider) ,
			StorageBackupItemQueue.New()
		);
	}
	
	public final class Default implements StorageBackupSetup
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageBackupFileProvider backupFileProvider;
		private final StorageBackupItemQueue    itemQueue         ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final StorageBackupFileProvider backupFileProvider,
			final StorageBackupItemQueue    itemQueue
		)
		{
			super();
			this.backupFileProvider = backupFileProvider;
			this.itemQueue          = itemQueue         ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final StorageBackupFileProvider backupFileProvider()
		{
			return this.backupFileProvider;
		}
		
		@Override
		public StorageFileWriter.Provider setupWriterProvider(
			final StorageFileWriter.Provider writerProvider
		)
		{
			return StorageFileWriterBackupping.Provider(this.itemQueue, writerProvider);
		}
		
		@Override
		public StorageBackupHandler setupHandler(
			final StorageOperationController       operationController,
			final StorageWriteController           writeController    ,
			final StorageDataFileValidator.Creator validatorCreator
		)
		{
			final int channelCount = operationController.channelCountProvider().getChannelCount();
			return StorageBackupHandler.New(
				this               ,
				channelCount       ,
				this.itemQueue     ,
				operationController,
				writeController    ,
				validatorCreator
			);
		}
		
	}
	
}
