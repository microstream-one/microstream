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

import static one.microstream.X.notNull;

import org.slf4j.Logger;

import one.microstream.X;
import one.microstream.afs.types.AFS;
import one.microstream.afs.types.AFile;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.persistence.types.PersistenceTypeDictionaryExporter;
import one.microstream.persistence.types.PersistenceTypeDictionaryStorer;
import one.microstream.storage.exceptions.StorageExceptionBackup;
import one.microstream.storage.exceptions.StorageExceptionBackupCopying;
import one.microstream.storage.exceptions.StorageExceptionBackupEmptyStorageBackupAhead;
import one.microstream.storage.exceptions.StorageExceptionBackupEmptyStorageForNonEmptyBackup;
import one.microstream.storage.exceptions.StorageExceptionBackupInconsistentFileLength;
import one.microstream.storage.types.StorageBackupHandler.Default.ChannelInventory;
import one.microstream.util.logging.Logging;

public interface StorageBackupHandler extends Runnable, StorageActivePart
{
	public StorageBackupSetup setup();
	
	public void initialize(int channelIndex);
	
	public void synchronize(StorageInventory storageInventory);
	
	public void copyFilePart(
		StorageLiveChannelFile<?> sourceFile    ,
		long                      sourcePosition,
		long                      length
	);
	
	public void truncateFile(
		StorageLiveChannelFile<?> file     ,
		long                      newLength
	);
	
	public void deleteFile(
		StorageLiveChannelFile<?> file
	);
	
	public StorageBackupHandler start();
	
	public default StorageBackupHandler stop()
	{
		this.setRunning(false);
		return this;
	}
	
	public boolean isRunning();
	
	@Override
	public boolean isActive();
	
	public StorageBackupHandler setRunning(boolean running);
	
	
	
	public static StorageBackupHandler New(
		final StorageBackupSetup               backupSetup        ,
		final int                              channelCount       ,
		final StorageBackupItemQueue           itemQueue          ,
		final StorageOperationController       operationController,
		final StorageWriteController           writeController    ,
		final StorageDataFileValidator.Creator validatorCreator   ,
		final StorageTypeDictionary            typeDictionary
	)
	{
		final StorageBackupFileProvider backupFileProvider = backupSetup.backupFileProvider();
		
		final ChannelInventory[] cis = X.Array(ChannelInventory.class, channelCount, i ->
		{
			return new ChannelInventory(i, backupFileProvider);
		});
		
		return new StorageBackupHandler.Default(
			        cis                 ,
			notNull(backupSetup)        ,
			notNull(itemQueue)          ,
			notNull(operationController),
			notNull(writeController)    ,
			notNull(validatorCreator)   ,
			notNull(typeDictionary)
		);
	}
	
	public final class Default implements StorageBackupHandler, StorageBackupInventory, PersistenceTypeDictionaryStorer
	{
		private final static Logger logger = Logging.getLogger(Default.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageBackupSetup                backupSetup           ;
		private final ChannelInventory[]                channelInventories    ;
		private final StorageBackupItemQueue            itemQueue             ;
		private final StorageOperationController        operationController   ;
		private final StorageWriteController            writeController       ;
		private final StorageDataFileValidator.Creator  validatorCreator      ;
		private final StorageTypeDictionary             typeDictionary        ;
		private final PersistenceTypeDictionaryExporter typeDictionaryExporter;
		
		private boolean running; // being "ordered" to run.
		private boolean active ; // being actually active, e.g. executing the last loop before running check.
		private boolean shutdown;// being "ordered" to stop the backup handler after completing current queued items
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final ChannelInventory[]               channelInventories ,
			final StorageBackupSetup               backupSetup        ,
			final StorageBackupItemQueue           itemQueue          ,
			final StorageOperationController       operationController,
			final StorageWriteController           writeController    ,
			final StorageDataFileValidator.Creator validatorCreator   ,
			final StorageTypeDictionary            typeDictionary
		)
		{
			super();
			this.channelInventories     = channelInventories ;
			this.backupSetup            = backupSetup        ;
			this.itemQueue              = itemQueue          ;
			this.operationController    = operationController;
			this.writeController        = writeController    ;
			this.validatorCreator       = validatorCreator   ;
			this.typeDictionary         = typeDictionary     ;
			
			this.typeDictionaryExporter = PersistenceTypeDictionaryExporter.New(this);
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final StorageBackupSetup setup()
		{
			return this.backupSetup;
		}
		
		@Override
		public final synchronized boolean isRunning()
		{
			return this.running && !(this.shutdown && this.itemQueue.isEmpty());
		}
		
		@Override
		public final synchronized boolean isActive()
		{
			return this.active;
		}
		
		@Override
		public final StorageBackupHandler start()
		{
			this.ensureTypeDictionaryBackup();
			this.setRunning(true);
			return this;
		}
		
		/**
		 * Initiate a controlled shutdown of the StorageBackupHandler
		 * after processing all currently enqueued items.
		 */
		@Override
		public synchronized StorageBackupHandler stop()
		{
			this.shutdown = true;
			return this;
		}
		
		@Override
		public final synchronized StorageBackupHandler setRunning(final boolean running)
		{
			this.running = running;
			return this;
		}
		
		@Override
		public StorageBackupDataFile ensureDataFile(final StorageDataFile file)
		{
			return this.channelInventories[file.channelIndex()].ensureBackupFile(file);
		}
		
		@Override
		public StorageBackupTransactionsFile ensureTransactionsFile(final StorageTransactionsFile file)
		{
			// "There can be only one..." (per channel)
			return this.channelInventories[file.channelIndex()].ensureTransactionsFile();
		}

		@Override
		public void initialize(final int channelIndex)
		{
			logger.debug("Initializing backup for channel #{}", channelIndex);
			
			try
			{
				this.tryInitialize(channelIndex);
			}
			catch(final RuntimeException e)
			{
				this.operationController.registerDisruption(e);
				throw e;
			}
		}
		
		@Override
		public void synchronize(final StorageInventory storageInventory)
		{
			logger.debug("Synchronizing backup with storage");
			
			try
			{
				this.trySynchronize(storageInventory);
				
				logger.debug("Backup synchronized successfully");
			}
			catch(final RuntimeException e)
			{
				this.operationController.registerDisruption(e);
				throw e;
			}
		}
		
		@Override
		public void storeTypeDictionary(final String typeDictionaryString)
		{
			this.setup().backupFileProvider().provideTypeDictionaryIoHandler().storeTypeDictionary(typeDictionaryString);
		}
		
		@Override
		public void run()
		{
			logger.info("Starting backup handler");
			
			// must be the method instead of the field to check the lock but don't cover the whole loop
			try
			{
				this.active = true;
				
				// can not / may not copy storage files if the storage is not running (has locked and opened files, etc.)
				while(this.isRunning() && this.operationController.checkProcessingEnabled())
				{
					try
					{
						this.itemQueue.processNextItem(this, 10_000);
					}
					catch(final InterruptedException e)
					{
						// still not sure about the viability of interruption handling in a case like this.
						this.stop();
					}
					catch(final RuntimeException e)
					{
						this.operationController.registerDisruption(e);
						// see outer try-finally for cleanup
						throw e;
					}
				}
			}
			finally
			{
				// must close all open files on any aborting case (after stopping and before throwing an exception)
				this.closeAllDataFiles();
				this.active = false;
				
				logger.info("Backup handler stopped");
			}
			
		}
		
		private void ensureTypeDictionaryBackup()
		{
			if(!this.setup().backupFileProvider().provideTypeDictionaryFile().exists())
			{
				logger.debug("Creating new type dictionary backup");
				this.typeDictionaryExporter.exportTypeDictionary(this.typeDictionary);
			}
			else
			{
				logger.debug("Existing type dictionary backup found");
			}
		}
		
		private void tryInitialize(final int channelIndex)
		{
			final ChannelInventory backupInventory = this.channelInventories[channelIndex];
			backupInventory.ensureRegisteredFiles();
		}
		
		private void trySynchronize(final StorageInventory storageInventory)
		{
			final ChannelInventory backupInventory = this.channelInventories[storageInventory.channelIndex()];
			if(backupInventory.dataFiles.isEmpty())
			{
				this.fillEmptyBackup(storageInventory, backupInventory);
			}
			else
			{
				this.updateExistingBackup(storageInventory, backupInventory);
			}
		}
		
		final void fillEmptyBackup(
			final StorageInventory storageInventory,
			final ChannelInventory backupInventory
		)
		{
			for(final StorageDataInventoryFile storageFile : storageInventory.dataFiles().values())
			{
				final StorageBackupDataFile backupTargetFile = storageFile.ensureBackupFile(this);
				this.copyFile(storageFile, backupTargetFile);
			}
			
			final StorageLiveTransactionsFile transactionFile = storageInventory.transactionsFileAnalysis().transactionsFile();
			final StorageBackupTransactionsFile backupTransactionFile = transactionFile.ensureBackupFile(this);
			this.copyFile(transactionFile, backupTransactionFile);
		}
		
		private void validateStorageInventoryForExistingBackup(
			final StorageInventory storageInventory,
			final ChannelInventory backupInventory
		)
		{
			if(!storageInventory.dataFiles().isEmpty())
			{
				return;
			}
			
			throw new StorageExceptionBackupEmptyStorageForNonEmptyBackup(
				backupInventory.channelIndex(),
				backupInventory.dataFiles()
			);
		}
		
		private void validateBackupFileProgress(
			final StorageInventory storageInventory,
			final ChannelInventory backupInventory
		)
		{
			final long lastStorageFileNumber = storageInventory.dataFiles().keys().last();
			final long lastBackupFileNumber  = backupInventory.dataFiles().keys().last();
			
			if(lastBackupFileNumber <= lastStorageFileNumber)
			{
				return;
			}
			
			throw new StorageExceptionBackupEmptyStorageBackupAhead(
				storageInventory,
				backupInventory.dataFiles()
			);
		}
		
		final void updateExistingBackup(
			final StorageInventory storageInventory,
			final ChannelInventory backupInventory
		)
		{
			this.validateStorageInventoryForExistingBackup(storageInventory, backupInventory);
			this.validateBackupFileProgress(storageInventory, backupInventory);

			final long lastBackupFileNumber = backupInventory.dataFiles().keys().last();
			for(final StorageDataInventoryFile dataFile : storageInventory.dataFiles().values())
			{
				final StorageBackupDataFile backupTargetFile = dataFile.ensureBackupFile(this);
				
				// non-existent files have either not been backupped, yet, or a "healable" error.
				if(!backupTargetFile.exists())
				{
					// in any case, the storage file is simply copied (backed up)
					this.copyFile(dataFile, backupTargetFile);
					continue;
				}
				
				final long storageFileLength      = dataFile.size();
				final long backupTargetFileLength = backupTargetFile.size();
				
				// existing file with matching length means everything is fine
				if(storageFileLength == backupTargetFileLength)
				{
					// continue with next file
					continue;
				}

				// the last/latest/highest existing backup file can validly diverge in length.
				if(backupTargetFile.number() == lastBackupFileNumber)
				{
					if(storageFileLength > backupTargetFileLength)
					{
						// missing length is copied to update the backup file only if the amount of bytes is greater then zero
						this.copyFilePart(
							dataFile,
							backupTargetFileLength,
							storageFileLength - backupTargetFileLength,
							backupTargetFile
						);
					}
					
					//if the backup target is larger we don't need to correct it here. It will be truncated later on.
					continue;
				}
				
				//a 0-Byte sized backup file can be updated
				if(backupTargetFile.number() > lastBackupFileNumber && backupTargetFileLength == 0)
				{
					// missing length is copied to update the backup file
					this.copyFilePart(
						dataFile,
						backupTargetFileLength,
						storageFileLength - backupTargetFileLength,
						backupTargetFile
					);
					continue;
				}
				
				// any existing non-last backup file with divergent length is a consistency error
				throw new StorageExceptionBackupInconsistentFileLength(
					storageInventory           ,
					backupInventory.dataFiles(),
					dataFile                ,
					storageFileLength          ,
					backupTargetFile           ,
					backupTargetFileLength
				);
			}
			
			this.synchronizeTransactionFile(storageInventory, backupInventory);
		}
		
		private void deleteBackupTransactionFile(final ChannelInventory backupInventory)
		{
			final StorageBackupTransactionsFile backupTransactionFile = backupInventory.transactionFile;
			if(backupTransactionFile == null || !backupTransactionFile.exists())
			{
				return;
			}
			
			// (12.08.2020 TM)FIXME: priv#351: control backup transaction file deletion?
			
			final AFile deletionTargetFile = this.backupSetup.backupFileProvider()
				.provideDeletionTargetFile(backupTransactionFile)
			;
			
			if(deletionTargetFile == null)
			{
				backupTransactionFile.delete();
			}
			else
			{
				AFS.executeWriting(deletionTargetFile, (wf) ->
				{
					backupTransactionFile.moveTo(wf);
				});
			}
			//remove deleted file from inventory
			backupInventory.transactionFile = null;
		}
		
		
		
		private void synchronizeTransactionFile(
			final StorageInventory storageInventory,
			final ChannelInventory backupInventory
		)
		{
			// tfa null means there is no transactions file. A non-existing transactions file later on is an error.
			final StorageTransactionsAnalysis tfa = storageInventory.transactionsFileAnalysis();
			if(tfa == null)
			{
				this.deleteBackupTransactionFile(backupInventory);
				return;
			}
			
			final StorageLiveTransactionsFile   liveTransactionsFile  = tfa.transactionsFile();
			final StorageBackupTransactionsFile backupTransactionFile = liveTransactionsFile.ensureBackupFile(this);
			
			if(!backupTransactionFile.exists())
			{
				// if the backup transaction file does not exist, yet, the actual file is simply copied.
				this.copyFile(liveTransactionsFile, backupTransactionFile);
				return;
			}

			final long storageFileLength      = liveTransactionsFile.size();
			final long backupTargetFileLength = backupTransactionFile.size();
			
			if(backupTargetFileLength != storageFileLength)
			{
				// on any mismatch, the backup transaction file is deleted (potentially moved&renamed) and rebuilt.
				this.deleteBackupTransactionFile(backupInventory);
				final StorageBackupTransactionsFile backupTransactionFileNew = liveTransactionsFile.ensureBackupFile(this);
				this.copyFile(liveTransactionsFile, backupTransactionFileNew);
			}
		}
				
		private void copyFile(
			final StorageFile       storageFile     ,
			final StorageBackupFile backupTargetFile
		)
		{
			storageFile.copyTo(backupTargetFile);
		}
		
		private void copyFilePart(
			final StorageChannelFile sourceFile      ,
			final long               sourcePosition  ,
			final long               length          ,
			final StorageBackupFile  backupTargetFile
		)
		{
			try
			{
				final long oldBackupFileLength = backupTargetFile.size();
				
				try
				{
					sourceFile.copyTo(backupTargetFile, sourcePosition, length);
					
					// (16.06.2020 TM)TODO: nasty instanceof
					if(backupTargetFile instanceof StorageBackupDataFile)
					{
						this.validatorCreator.createDataFileValidator()
							.validateFile((StorageBackupDataFile)backupTargetFile, oldBackupFileLength, length);
					}
				}
				catch(final Exception e)
				{
					throw new StorageExceptionBackupCopying(sourceFile, sourcePosition, length, backupTargetFile, e);
				}
				finally
				{
					backupTargetFile.close();
				}
			}
			catch(final Exception e)
			{
				throw new StorageExceptionBackupCopying(sourceFile, sourcePosition, length, backupTargetFile, e);
			}
		}
		
		@Override
		public void copyFilePart(
			final StorageLiveChannelFile<?> sourceFile    ,
			final long                      sourcePosition,
			final long                      copyLength
		)
		{
			// note: the original target file of the copying is irrelevant. Only the backup target file counts.
			final StorageBackupFile backupTargetFile = sourceFile.ensureBackupFile(this);
			
			logger.debug(
				"Copying backup file part from {}, length {}: {}",
				sourcePosition,
				copyLength,
				backupTargetFile.file().toPathString()
			);
			
			this.copyFilePart(sourceFile, sourcePosition, copyLength, backupTargetFile);
		}

		@Override
		public void truncateFile(
			final StorageLiveChannelFile<?> file     ,
			final long                      newLength
		)
		{
			final StorageBackupChannelFile backupTargetFile = file.ensureBackupFile(this);
			
			logger.debug(
				"Truncating backup file to {} bytes: {}",
				newLength,
				backupTargetFile.file().toPathString()
			);
			
			StorageFileWriter.truncateFile(backupTargetFile, newLength, this.backupSetup.backupFileProvider());
			
			// no user decrement since only the identifier is required and the actual file can well have been deleted.
		}
		
		@Override
		public void deleteFile(final StorageLiveChannelFile<?> file)
		{
			if(!this.writeController.isFileDeletionEnabled())
			{
				return;
			}
			
			final StorageBackupChannelFile backupTargetFile = file.ensureBackupFile(this);
			
			logger.debug("Deleting backup file: {}", backupTargetFile.file().toPathString());
			
			StorageFileWriter.deleteFile(
				backupTargetFile,
				this.writeController,
				this.backupSetup.backupFileProvider()
			);
			
			// no user decrement since only the identifier is required and the actual file can well have been deleted.
		}
		
		final void closeAllDataFiles()
		{
			final DisruptionCollectorExecuting<StorageClosableFile> closer = DisruptionCollectorExecuting.New(file ->
				file.close()
			);
			
			for(final ChannelInventory channel : this.channelInventories)
			{
				closer.executeOn(channel.transactionFile);
				for(final StorageBackupDataFile dataFile : channel.dataFiles.values())
				{
					closer.executeOn(dataFile);
				}
			}
			
			if(closer.hasDisruptions())
			{
				throw new StorageExceptionBackup(closer.toMultiCauseException());
			}
		}
		
		
		
		static final class ChannelInventory implements StorageHashChannelPart
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final int                                      channelIndex      ;
			final StorageBackupFileProvider                backupFileProvider;
			      StorageBackupTransactionsFile            transactionFile   ;
			      EqHashTable<Long, StorageBackupDataFile> dataFiles         ;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			ChannelInventory(
				final int                       channelIndex      ,
				final StorageBackupFileProvider backupFileProvider
			)
			{
				super();
				this.channelIndex       = channelIndex      ;
				this.backupFileProvider = backupFileProvider;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public final int channelIndex()
			{
				return this.channelIndex;
			}
			
			public final EqHashTable<Long, StorageBackupDataFile> dataFiles()
			{
				return this.dataFiles;
			}
			
			final void ensureRegisteredFiles()
			{
				this.ensureDataFiles();
				this.ensureTransactionsFile();
			}
			
			final StorageBackupTransactionsFile ensureTransactionsFile()
			{
				if(this.transactionFile == null)
				{
					final StorageBackupTransactionsFile transactionsfile =
						this.backupFileProvider.provideBackupTransactionsFile(this.channelIndex())
					;
					
					// ensure file existence before mutating instance state.
					transactionsfile.file().ensureExists();
					
					this.transactionFile = transactionsfile;
				}
				
				return this.transactionFile;
			}
			
			
			final StorageBackupDataFile ensureBackupFile(final StorageDataFile sourceFile)
			{
				// note: validation is done by the calling context, depending on its task.
				
				StorageBackupDataFile backupFile = this.dataFiles.get(sourceFile.number());
				if(backupFile == null)
				{
					backupFile = this.backupFileProvider.provideBackupDataFile(sourceFile);
					
					// ensure file existence before mutating instance state.
					backupFile.file().ensureExists();
					
					this.registerBackupFile(backupFile);
				}
				
				return backupFile;
			}
			
			private StorageBackupDataFile registerBackupFile(final StorageBackupDataFile backupFile)
			{
				this.dataFiles.add(backupFile.number(), backupFile);
				
				return backupFile;
			}
			
			final void ensureDataFiles()
			{
				if(this.dataFiles != null)
				{
					// files already registered
					return;
				}
				
				final BulkList<StorageBackupDataFile> collectedFiles =
				this.backupFileProvider.collectDataFiles(
					StorageBackupDataFile::New,
					BulkList.New(),
					this.channelIndex()
				)
				.sort(StorageDataFile::orderByNumber);
				
				this.dataFiles = EqHashTable.New();
				
				collectedFiles.iterate(this::registerBackupFile);
			}
			
		}
		
	}
	
}
