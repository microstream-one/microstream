package one.microstream.storage.types;

import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;

import one.microstream.afs.types.AFile;
import one.microstream.util.logging.Logging;

/*-
 * #%L
 * MicroStream Storage
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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

public interface StorageStartupIndexManager
{
	/**
	 * Create startup index for the supplied StorageLiveDataFile if
	 * not existing or out of date.
	 * 
	 * @param storageLiveFile StorageLiveDataFile to be indexed.
	 */
	public void indexFile(StorageLiveDataFile storageLiveFile);
	
	/**
	 * Create startup index for all supplied storage StorageLiveDataFiles
	 * supplied by the StorageFileManager if not existing or out of date.
	 * 
	 * @param fileManager that provides all storage files.
	 */
	public void createIndex(StorageFileManager.Default fileManager);
		
	/**
	 * get the StorageStartupIndexFile for the supplied StorageLiveDataFile
	 * 
	 * @param storageLiveFile StorageLiveDataFile
	 * @return StorageStartupIndexFile
	 */
	public StorageStartupIndexFile getIndexFileFor(StorageLiveDataFile storageLiveFile);
	
	/**
	 * delete the StorageStartupIndexFile related to the supplied StorageLiveDataFile
	 * 
	 * @param storageLiveFile StorageLiveDataFile
	 */
	public void deleteIndexFileFor(StorageLiveDataFile.Default storageLiveFile);

	/**
	 * Try to initialize storage entities using the StorageStartupIndexFile
	 * associated with the supplied StorageLiveDataFile if possible.
	 * If not possible the storage entities are initialized from the StorageLiveDataFile
	 * directly and a new StorageStartupIndexFile is created.
	 * 
	 * @param dataFile StorageLiveDataFile
	 * @param entityCache StorageEntityCache
	 * @param action fallback action to be executed if initialization from index file fails.
	 * By default, this is the initialization routine for initialization from a StorageLiveDataFile.
	 */
	public void initializeEntities(
			StorageLiveDataFile.Default dataFile,
			StorageEntityCache.Default entityCache,
			Consumer<StorageLiveDataFile.Default> action
			);
	
	
	/**
	 * Delete all obsolete index files.
	 * 
	 * @param fileManager the StorageFileManager
	 */
	public void cleanupIndexFiles(StorageFileManager.Default fileManager);

	/**
	 * Create a new StorageStartupIndexManager
	 * 
	 * @param indexFileProvider the StorageStartupIndexFileProvider
	 * @param indexer the StorageStartupFileIndexer
	 * @return a new StorageStartupIndexManager
	 */
	static StorageStartupIndexManager New
	(
		final StorageStartupIndexFileProvider indexFileProvider,
		final StorageStartupFileIndexer indexer) {
		
		return new StorageStartupIndexManager.Default(indexFileProvider, indexer);
	}

	public class Default implements StorageStartupIndexManager
	{
		private final static Logger logger = Logging.getLogger(StorageStartupFileIndexer.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageStartupIndexFileProvider indexFileProvider;
		private final StorageStartupFileIndexer       indexer;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default(final StorageStartupIndexFileProvider indexFileProvider, final StorageStartupFileIndexer indexer)
		{
			this.indexFileProvider = indexFileProvider;
			this.indexer = indexer;
		}

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void createIndex(final StorageFileManager.Default fileManager)
		{
			fileManager.iterateStorageFiles(
				this::indexFile
			);
		}

		@Override
		public StorageStartupIndexFile getIndexFileFor(final StorageLiveDataFile storageLiveFile)
		{
			final StorageStartupIndexFile indexFile = this.indexFileProvider.provideStartupIndexFile(storageLiveFile);
			if(indexFile.exists())
			{
				return indexFile;
			}
			return null;
			
		}


		@Override
		public void indexFile(final StorageLiveDataFile storageLiveDataFile)
		{
			final StorageStartupIndexFile indexFile = this.indexFileProvider.provideStartupIndexFile(storageLiveDataFile);
			if(indexFile.exists())
			{
				if(this.indexer.isUpToDate(storageLiveDataFile, indexFile))
				{
					logger.debug("index file {} is up to date", indexFile.identifier());
					return;
				}
				
				logger.debug("deleting index file {}; not up to date", indexFile.identifier());
				indexFile.delete();
			}
			
			
			try
			{
				indexFile.file().ensureExists();
				this.indexer.indexStorageFile(storageLiveDataFile, indexFile);
			}
			catch(final Exception e) // if indexing fails the storage is still operational, no need to abort
			{
				logger.error("Indexing failed for storage file " + storageLiveDataFile.identifier() + "!", e);
				indexFile.delete();
			}
		}


		@Override
		public void deleteIndexFileFor(final StorageLiveDataFile.Default file)
		{
			final StorageStartupIndexFile indexFile = this.getIndexFileFor(file);
			if(indexFile != null)
			{
				logger.debug("deleting index file {}", indexFile.identifier());
				indexFile.delete();
			}
		}


		@Override
		public void initializeEntities(
			final StorageLiveDataFile.Default dataFile,
			final StorageEntityCache.Default entityCache,
			final Consumer<StorageLiveDataFile.Default> action
		)
		{
			final StorageStartupIndexFile indexFile = this.getIndexFileFor(dataFile);
			if(indexFile != null)
			{
				if(this.indexer.isUpToDate(dataFile, indexFile))
				{
					try
					{
						this.indexer.initWithIndexFile(dataFile, indexFile, entityCache);
						return;
					}
					catch(final Exception e)
					{
						logger.error("Failed initialization from index file " + indexFile.identifier() + ", init with storage file instead", e);
						indexFile.delete();
					}
						
				}
				else
				{
					logger.debug("init from storage file {}. index file {} not up to date", dataFile.identifier() , indexFile.identifier());
				}
			}
			else
			{
				logger.warn("missing index file {} for channel {}, init from data file instead.",  dataFile.number(), dataFile.channelIndex());
			}
			
			action.accept(dataFile);
			
			this.indexFile(dataFile);
			
		}


		@Override
		public void cleanupIndexFiles(final one.microstream.storage.types.StorageFileManager.Default fileManager)
		{
			final List<AFile> indexFiles = this.indexFileProvider.getFiles(fileManager.channelIndex());
			
			fileManager.iterateStorageFiles(f -> {
				 final StorageStartupIndexFile indexFile = this.getIndexFileFor(f);
				 if(indexFile != null)
				 {
					 indexFiles.remove(indexFile.file());
				 }
			});
			
			indexFiles.forEach(f -> {
				logger.debug("deleting obsolete index file {}", f.name());
				f.tryUseWriting().delete();
			});
		}

	}

}
