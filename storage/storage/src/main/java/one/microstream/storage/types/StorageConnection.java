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

import java.nio.ByteBuffer;
import java.util.function.Predicate;

import one.microstream.afs.types.ADirectory;
import one.microstream.afs.types.AFile;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceTypeDictionaryExporter;
import one.microstream.persistence.types.Persister;
import one.microstream.persistence.types.Storer;
import one.microstream.persistence.types.Unpersistable;
import one.microstream.storage.exceptions.StorageExceptionBackupFullBackupTargetNotEmpty;


/**
 * Ultra-thin delegating type that connects the application context to a storage instance via a Persistence layer
 * (a {@link PersistenceManager} instance, potentially exclusively created).
 * <p>
 * Note that this is a rather "internal" type that users usually do not have to use or care about.
 * Since {@link StorageManager} implements this interface, is normally sufficient to use just that.
 *
 */
public interface StorageConnection extends Persister
{
	/* (11.05.2014 TM)TODO: Proper InterruptedException handling
	 *  just returning, especially returning null (see below) seems quite dangerous.
	 *  Research how to handle such cases properly.
	 *  Difficult: what to return if the thread has been aborted? Throw an exception?
	 *  Maybe set the thread's interrupted flag (seen once in an article)
	 */

	// (03.12.2014 TM)TODO: method to query the transactions files content because channels have a lock on it

	// currently only for type parameter fixation

	/**
	 * Issues a full garbage collection to be executed. Depending on the size of the database,
	 * the available cache, used hardware, etc., this can take any amount of time.
	 * <p>
	 * Garbage collection marks all persisted objects/records that are reachable from the root (mark phase)
	 * and once that is completed, all non-marked records are determined to be effectively unreachable
	 * and are thus deleted. This common mechanism in graph-organised data completely removes the need
	 * for any explicit deleting.
	 * <p>
	 * Note that the garbage collection on the storage level has nothing to do with the JVM's Garbage Collector
	 * on the heap level. While the technical principle is the same, both GCs are separate from each other and
	 * do not have anything to do with each other.
	 * 
	 * @see #issueGarbageCollection(long)
	 */
	public default void issueFullGarbageCollection()
	{
		this.issueGarbageCollection(Long.MAX_VALUE);
	}

	/**
	 * Issues garbage collection to be executed, limited to the time budget in nanoseconds specified
	 * by the passed {@code nanoTimeBudget}.<br>
	 * When the time budget is used up, the garbage collector will keep the current progress and continue there
	 * at the next opportunity. The same progress marker is used by the implicit housekeeping, so both mechanisms
	 * will continue on the same progress.<br>
	 * If no store has occurred since the last completed garbage sweep, this method will have no effect and return
	 * immediately.
	 * 
	 * @param nanoTimeBudget the time budget in nanoseconds to be used to perform garbage collection.
	 * 
	 * @return whether the returned call has completed garbage collection.
	 * 
	 * @see #issueFullGarbageCollection()
	 */
	public boolean issueGarbageCollection(long nanoTimeBudget);

	/**
	 * Issues a full storage file check to be executed. Depending on the size of the database,
	 * the available cache, used hardware, etc., this can take any amount of time.
	 * <p>
	 * File checking evaluates every storage data file about being either too small, too big
	 * or having too many logical "gaps" in it (created by storing newer versions of an object
	 * or by garbage collection). If one of those checks applies, the remaining live data in
	 * the file is moved to the current head file and once that is done, the source file
	 * (now consisting of 100% logical "gaps", making it effectively superfluous) is then deleted.
	 * <p>
	 * The exact logic is defined by {@link StorageConfiguration#dataFileEvaluator()}
	 * 
	 * @see #issueFileCheck(long)
	 */
	public default void issueFullFileCheck()
	{
		this.issueFileCheck(Long.MAX_VALUE);
	}

	/**
	 * Issues a storage file check to be executed, limited to the time budget in nanoseconds specified
	 * by the passed {@code nanoTimeBudget}.<br>
	 * When the time budget is used up, the checking logic will keep the current progress and continue there
	 * at the next opportunity. The same progress marker is used by the implicit housekeeping, so both mechanisms
	 * will continue on the same progress.<br>
	 * If no store has occurred since the last completed check, this method will have no effect and return
	 * immediately.
	 * 
	 * @param nanoTimeBudget the time budget in nanoseconds to be used to perform file checking.
	 * 
	 * @return whether the returned call has completed file checking.
	 */
	public boolean issueFileCheck(long nanoTimeBudget);

	/**
	 * Issues a full storage cache check to be executed. Depending on the size of the database,
	 * the available cache, used hardware, etc., this can take any amount of time.
	 * <p>
	 * Cache checking evaluates every cache entity data about being worth to be kept in cache according to
	 * the configured {@link StorageEntityCacheEvaluator} logic. If deemed unworthy, its data will be cleared
	 * from the cache and has to be loaded from the persistent form on the next reading access.<br>
	 * The check will run until the used cache size is 0 or every entity is checked at least once.
	 * 
	 * @see #issueFullCacheCheck(StorageEntityCacheEvaluator)
	 * @see #issueCacheCheck(long)
	 * @see #issueCacheCheck(long, StorageEntityCacheEvaluator)
	 */
	public default void issueFullCacheCheck()
	{
		this.issueFullCacheCheck(null); // providing no explicit evaluator means to use the internal one
	}

	/**
	 * Same as {@link #issueFullCacheCheck()}, but with using the passed {@link StorageEntityCacheEvaluator}
	 * logic instead of the configured one.
	 * 
	 * @param entityEvaluator the entity cache evaluation logic to be used for the call.
	 * 
	 * @see #issueFullCacheCheck()
	 * @see #issueCacheCheck(long)
	 * @see #issueCacheCheck(long, StorageEntityCacheEvaluator)
	 */
	public default void issueFullCacheCheck(final StorageEntityCacheEvaluator entityEvaluator)
	{
		this.issueCacheCheck(Long.MAX_VALUE, entityEvaluator);
	}

	/**
	 * Issues a storage cache check to be executed, limited to the time budget in nanoseconds specified
	 * by the passed {@code nanoTimeBudget}.<br>
	 * When the time budget is used up, the checking logic will keep the current progress and continue there
	 * at the next opportunity. The same progress marker is used by the implicit housekeeping, so both mechanisms
	 * will continue on the same progress.<br>
	 * If the used cache size is 0, this method will have no effect and return immediately.
	 * 
	 * @param nanoTimeBudget the time budget in nanoseconds to be used to perform cache checking.
	 * 
	 * @return whether the used cache size is 0 or became 0 via the performed check.
	 * 
	 * @see #issueFullCacheCheck()
	 * @see #issueFullCacheCheck(StorageEntityCacheEvaluator)
	 * @see #issueCacheCheck(long, StorageEntityCacheEvaluator)
	 */
	public default boolean issueCacheCheck(final long nanoTimeBudget)
	{
		return this.issueCacheCheck(nanoTimeBudget, null);
	}

	/**
	 * Same as {@link #issueCacheCheck(long)}, but with using the passed {@link StorageEntityCacheEvaluator}
	 * logic instead of the configured one.
	 * 
	 * @param nanoTimeBudget the time budget in nanoseconds to be used to perform cache checking.
	 * @param entityEvaluator the entity cache evaluation logic to be used for the call.
	 * 
	 * @return whether the used cache size is 0 or became 0 via the performed check.
	 * 
	 * @see #issueFullCacheCheck()
	 * @see #issueFullCacheCheck(StorageEntityCacheEvaluator)
	 * @see #issueCacheCheck(long)
	 */
	public boolean issueCacheCheck(long nanoTimeBudget, StorageEntityCacheEvaluator entityEvaluator);
	
	/**
	 * Issues a full backup of the whole storage to be executed. Keep in mind that this could result in a
	 * very long running operation, depending on the storage size.<br>
	 * Although the full backup may be a valid solution in some circumstances, the incremental backup should
	 * be preferred, since it is by far more efficient.
	 * <p>
	 * if the target is existing and not empty an {@link StorageExceptionBackupFullBackupTargetNotEmpty} exception
	 * will be thrown
	 * 
	 * @param targetDirectory the directory to write the backup data into
	 * 
	 * @since 04.01.00
	 */
	public default void issueFullBackup(final ADirectory targetDirectory)
	{
		if(!targetDirectory.exists() || targetDirectory.isEmpty())
			{
			this.issueFullBackup(
				StorageLiveFileProvider.New(targetDirectory),
				PersistenceTypeDictionaryExporter.New(
					PersistenceTypeDictionaryFileHandler.New(targetDirectory)
				)
			);
		}
		else
		{
			throw new StorageExceptionBackupFullBackupTargetNotEmpty(targetDirectory);
		}
	}
	
	/**
	 * Issues a full backup of the whole storage to be executed. Keep in mind that this could result in a
	 * very long running operation, depending on the storage size.<br>
	 * Although the full backup may be a valid solution in some circumstances, the incremental backup should
	 * be preferred, since it is by far more efficient.
	 * 
	 * @param targetFileProvider file provider for backup files
	 * @param typeDictionaryExporter custom type dictionary exporter
	 * 
	 * @since 04.01.00
	 */
	public void issueFullBackup(
		StorageLiveFileProvider           targetFileProvider    ,
		PersistenceTypeDictionaryExporter typeDictionaryExporter
	);

	/**
	 * Creates a {@link StorageRawFileStatistics} instance, (obviously) containing raw file statistics about
	 * every channel in the storage.
	 * 
	 * @return a {@link StorageRawFileStatistics} instance based on the current state.
	 */
	public StorageRawFileStatistics createStorageStatistics();

	/* (28.06.2013 TM)TODO: post-sweep-task queue?
	 * even more practical then or additional to the above would be to have a post-sweep task queue
	 * that gets executed automatically after a sweep is completed.
	 * That way, things like backups could be set up to occur automatically at the right time
	 * without having to actively poll (trial-and-error) for it.
	 * Should not be to complicated as the phase check already is a task
	 */
	
	// (23.06.2020 TM)FIXME: priv#49: switch #exportChannels to ExportFileProvider or such
	
	/**
	 * Exports the data of all channels in the storage by using the passed {@link StorageLiveFileProvider} instance.<br>
	 * This is basically a simple file copy applied to all files in the storage, however with the guaranteed safety
	 * of no other task / access to the storage's files intervening with the ongoing process. This is useful to
	 * safely create a complete copy of the storage, e.g. a full backup.
	 * 
	 * @param fileProvider the {@link StorageLiveFileProvider} logic to be used for the export.
	 * @param performGarbageCollection whether a {@link #issueFullGarbageCollection()} shall be issued before
	 *        performing the export.
	 */
	public void exportChannels(StorageLiveFileProvider fileProvider, boolean performGarbageCollection);

	/**
	 * Alias for {@code this.exportChannels(fileHandler, true);}.
	 * 
	 * @param fileProvider the {@link StorageLiveFileProvider} logic to be used for the export.
	 * 
	 * @see #exportChannels(StorageLiveFileProvider, boolean)
	 */
	public default void exportChannels(final StorageLiveFileProvider fileProvider)
	{
		this.exportChannels(fileProvider, true);
	}
	
	/**
	 * Exports the entity data of all selected types of all channels into one file per type.<br>
	 * The data will be in the native binary format used internally by the storage. Converters can be used
	 * to transform the data into a different, for example human readable, form like CSV.
	 * <p>
	 * This is useful to extract the data contained in the storage in a structured way, for example to migrate it
	 * into another storage system or to analyze it, like converting it into human readable form.
	 * 
	 * @param exportFileProvider the {@link StorageEntityTypeExportFileProvider} logic to be used.
	 * 
	 * @param isExportType a {@link Predicate} selecting which type's entity data to be exported.
	 * 
	 * @return a {@link StorageEntityTypeExportStatistics} information instance about the completed export.
	 * 
	 * @see #exportTypes(StorageEntityTypeExportFileProvider)
	 */
	public StorageEntityTypeExportStatistics exportTypes(
		StorageEntityTypeExportFileProvider         exportFileProvider,
		Predicate<? super StorageEntityTypeHandler> isExportType
	);

	/**
	 * Alias for {@code this.exportTypes(exportFileProvider, null);}, meaning all types are exported.
	 * 
	 * @param exportFileProvider the {@link StorageEntityTypeExportFileProvider} logic to be used.
	 * 
	 * @return a {@link StorageEntityTypeExportStatistics} information instance about the completed export.
	 * 
	 * @see #exportTypes(StorageEntityTypeExportFileProvider, Predicate)
	 */
	public default StorageEntityTypeExportStatistics exportTypes(
		final StorageEntityTypeExportFileProvider exportFileProvider
	)
	{
		return this.exportTypes(exportFileProvider, null);
	}

	/**
	 * Imports all files specified by the passed Enum (ordered set) of {@link AFile} in order.<br>
	 * The files are assumed to be in the native binary format used internally by the storage.<br>
	 * All entities contained in the specified files will be imported. If they already exist in the storage
	 * (identified by their ObjectId), their current data will be replaced by the imported data.<br>
	 * Note that importing data that is not reachable from any root entity will have no effect and will
	 * eventually be deleted by the garbage collector.
	 * 
	 * @param importFiles the files whose native binary content shall be imported.
	 */
	public void importFiles(XGettingEnum<AFile> importFiles);

	/**
	 * Imports all data specified by the passed Enum (ordered set) of {@link ByteBuffer} in order.<br>
	 * The buffers are assumed to be in the native binary format used internally by the storage.<br>
	 * All entities contained in the specified buffers will be imported. If they already exist in the storage
	 * (identified by their ObjectId), their current data will be replaced by the imported data.<br>
	 * Note that importing data that is not reachable from any root entity will have no effect and will
	 * eventually be deleted by the garbage collector.
	 * 
	 * @param importData the files whose native binary content shall be imported.
	 */
	public void importData(XGettingEnum<ByteBuffer> importData);

	/**
	 * @return the {@link PersistenceManager} used by this {@link StorageConnection}.
	 */
	public PersistenceManager<Binary> persistenceManager();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public default long store(final Object instance)
	{
		return this.persistenceManager().store(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public default long[] storeAll(final Object... instances)
	{
		return this.persistenceManager().storeAll(instances);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public default void storeAll(final Iterable<?> instances)
	{
		this.persistenceManager().storeAll(instances);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public default Storer createLazyStorer()
	{
		return this.persistenceManager().createLazyStorer();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public default Storer createStorer()
	{
		return this.persistenceManager().createStorer();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public default Storer createEagerStorer()
	{
		return this.persistenceManager().createEagerStorer();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public default Object getObject(final long objectId)
	{
		return this.persistenceManager().getObject(objectId);
	}


	
	public static StorageConnection New(
		final PersistenceManager<Binary> persistenceManager       ,
		final StorageRequestAcceptor     connectionRequestAcceptor
	)
	{
		return new StorageConnection.Default(
			notNull(persistenceManager)       ,
			notNull(connectionRequestAcceptor)
		);
	}
	

	public final class Default implements StorageConnection, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceManager<Binary> persistenceManager       ;
		private final StorageRequestAcceptor     connectionRequestAcceptor;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceManager<Binary> persistenceManager       ,
			final StorageRequestAcceptor     connectionRequestAcceptor
		)
		{
			super();
			this.persistenceManager        = persistenceManager       ;
			this.connectionRequestAcceptor = connectionRequestAcceptor;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceManager<Binary> persistenceManager()
		{
			return this.persistenceManager;
		}

		@Override
		public final boolean issueGarbageCollection(final long nanoTimeBudget)
		{
			try
			{
				// a time budget <= 0 will effectively be a cheap query for the completion state.
				return this.connectionRequestAcceptor.issueGarbageCollection(nanoTimeBudget);
			}
			catch(final InterruptedException e)
			{
				// thread interrupted, task aborted, return
				return false;
			}
		}

		@Override
		public final boolean issueFileCheck(final long nanoTimeBudget)
		{
			try
			{
				// a time budget <= 0 will effectively be a cheap query for the completion state.
				return this.connectionRequestAcceptor.issueFileCheck(nanoTimeBudget);
			}
			catch(final InterruptedException e)
			{
				// thread interrupted, task aborted, return
				return false;
			}
		}

		@Override
		public final boolean issueCacheCheck(
			final long                        nanoTimeBudget,
			final StorageEntityCacheEvaluator entityEvaluator
		)
		{
			try
			{
				// a time budget <= 0 will effectively be a cheap query for the completion state.
				return this.connectionRequestAcceptor.issueCacheCheck(nanoTimeBudget, entityEvaluator);
			}
			catch(final InterruptedException e)
			{
				// thread interrupted, task aborted, return
				return false;
			}
		}
		
		@Override
		public final void issueFullBackup(
			final StorageLiveFileProvider           targetFileProvider    ,
			final PersistenceTypeDictionaryExporter typeDictionaryExporter
		)
		{
			this.exportChannels(targetFileProvider);
			typeDictionaryExporter.exportTypeDictionary(this.persistenceManager().typeDictionary());
		}

		@Override
		public StorageRawFileStatistics createStorageStatistics()
		{
			try
			{
				return this.connectionRequestAcceptor.createStatistics();
			}
			catch(final InterruptedException e)
			{
				// thread interrupted, task aborted, return
				return null;
			}
		}

		@Override
		public void exportChannels(final StorageLiveFileProvider fileProvider, final boolean performGarbageCollection)
		{
			try
			{
				this.connectionRequestAcceptor.exportChannels(fileProvider, performGarbageCollection);
			}
			catch(final InterruptedException e)
			{
				// thread interrupted, task aborted, return
				return;
			}
		}

		@Override
		public StorageEntityTypeExportStatistics exportTypes(
			final StorageEntityTypeExportFileProvider         exportFileProvider,
			final Predicate<? super StorageEntityTypeHandler> isExportType
		)
		{
			try
			{
				return this.connectionRequestAcceptor.exportTypes(exportFileProvider, isExportType);
			}
			catch(final InterruptedException e)
			{
				// thread interrupted, task aborted, return
				return null;
			}
		}

		@Override
		public void importFiles(final XGettingEnum<AFile> importFiles)
		{
			try
			{
				this.connectionRequestAcceptor.importFiles(importFiles);
			}
			catch(final InterruptedException e)
			{
				// thread interrupted, task aborted, return
				return;
			}
		}

		@Override
		public void importData(final XGettingEnum<ByteBuffer> importData)
		{
			try
			{
				this.connectionRequestAcceptor.importData(importData);
			}
			catch(final InterruptedException e)
			{
				// thread interrupted, task aborted, return
				return;
			}
		}

	}

}
