package net.jadoth.storage.types;

import static net.jadoth.Jadoth.notNull;

import java.io.File;
import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceManager;
import net.jadoth.persistence.types.PersistenceStoring;
import net.jadoth.persistence.types.SelfStoring;
import net.jadoth.persistence.types.Storer;
import net.jadoth.persistence.types.Unpersistable;


/**
 * Ultra-thin delegatig type that connects a {@link PersistenceManager} instance (potentially exclusively created)
 * to a storage instace.
 *
 * @author TM
 */
public interface StorageConnection extends PersistenceStoring
{
	/* (11.05.2014)TODO: Proper InterruptedException handling
	 *  just returning, especially returning null (see below) seems quite dangerous.
	 *  Research how to handle such cases properly.
	 *  Difficult: what to return if the thread has been aborted? Throw an exception?
	 *  Maybe set the thread's interrupted flag (seen once in an article)
	 */

	// (03.12.2014)TODO: method to query the transactions files content because channels have a lock on it

	// currently only for type parameter fixation

	/**
	 * Issues a full garbage collection to be executed. Depending on the size of the database,
	 * the available cache, used hardware, etc., this can take any amount of time.
	 */
	public default void issueFullGarbageCollection()
	{
		this.issueGarbageCollection(Long.MAX_VALUE);
	}

	public boolean issueGarbageCollection(long nanoTimeBudget);

	public default void issueFullFileCheck()
	{
		this.issueFullFileCheck(null); // providing no explicit evaluator means to use the internal one
	}

	public default void issueFullFileCheck(final StorageDataFileDissolvingEvaluator fileDissolvingEvaluator)
	{
		 this.issueFileCheck(Long.MAX_VALUE, fileDissolvingEvaluator);
	}

	public default boolean issueFileCheck(final long nanoTimeBudgetBound)
	{
		return this.issueFileCheck(nanoTimeBudgetBound, null);
	}

	public boolean issueFileCheck(long nanoTimeBudgetBound, StorageDataFileDissolvingEvaluator fileDissolvingEvaluator);

	public default void issueFullCacheCheck()
	{
		this.issueFullCacheCheck(null); // providing no explicit evaluator means to use the internal one
	}

	public default void issueFullCacheCheck(final StorageEntityCacheEvaluator entityEvaluator)
	{
		this.issueCacheCheck(Long.MAX_VALUE, entityEvaluator);
	}

	public default boolean issueCacheCheck(final long nanoTimeBudgetBound)
	{
		return this.issueCacheCheck(nanoTimeBudgetBound, null);
	}

	public boolean issueCacheCheck(long nanoTimeBudgetBound, StorageEntityCacheEvaluator entityEvaluator);

	public StorageRawFileStatistics createStorageStatistics();

	/* (28.06.2013 TM)TODO: post-sweep-task queue?
	 * even more practical then or additional to the above would be to have a post-sweep task queue
	 * that gets executed automatically after a sweep is completed.
	 * That way, things like backups could be set up to occur automatically at the right time
	 * without having to actively poll (trial-and-error) for it.
	 * Should not be to complicated as the phase check already is a task
	 */
	public void exportChannels(StorageIoHandler fileHandler, boolean performGarbageCollection);

	public default void exportChannels(final StorageIoHandler fileHandler)
	{
		this.exportChannels(fileHandler, true);
	}

	public default StorageEntityTypeExportStatistics exportTypes(
		final StorageEntityTypeExportFileProvider exportFileProvider
	)
	{
		return this.exportTypes(exportFileProvider, null);
	}
	
	public StorageEntityTypeExportStatistics exportTypes(
		StorageEntityTypeExportFileProvider            exportFileProvider,
		Predicate<? super StorageEntityTypeHandler<?>> isExportType
	);
	

	public void importFiles(XGettingEnum<File> importFiles);

	/* (13.07.2015)TODO: load by type somehow
	 * Query by typeId already implemented. Question is how to best provide it to the user.
	 * As a result HashTable or Sequence?
	 * By class or by type id or both?
	 */

//	public XGettingTable<Class<?>, ? extends XGettingEnum<?>> loadAllByTypes(XGettingEnum<Class<?>> types);


	public PersistenceManager<Binary> persistenceManager();

	/**
	 * @deprecated To be removed soon and replaced by a way to create different types of storers.
	 */
	@Deprecated
	@Override
	public default long[] storeAllFull(final Object... instances)
	{
		return this.persistenceManager().storeAllFull(instances);
	}
	
	/**
	 * @deprecated To be removed soon. Use the semantically identical {@link #storeAll(Object...)} instead.
	 */
	@Deprecated
	@Override
	public default long[] storeAllRequired(final Object... instances)
	{
		return this.persistenceManager().storeAllRequired(instances);
	}

	/**
	 * @deprecated To be removed soon and replaced by a way to create different types of storers.
	 */
	@Deprecated
	@Override
	public default long storeFull(final Object instance)
	{
		return this.persistenceManager().storeFull(instance);
	}

	/**
	 * @deprecated To be removed soon. Use the semantically identical {@link #store(Object)} instead.
	 */
	@Deprecated
	@Override
	public default long storeRequired(final Object instance)
	{
		return this.persistenceManager().storeRequired(instance);
	}

	public default void store(final SelfStoring storing)
	{
		storing.storeBy(this.createStorer()).commit();
	}

	public default Storer createStorer()
	{
		return this.persistenceManager().createStorer();
	}



	public final class Implementation implements StorageConnection, Unpersistable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		/* The performance penalty of this indirection is negligible as a persistence manager instance
		 * is only (properly) used for non-performance-relevant uses and otherwise spawns dedicated
		 * storer/loader instances.
		 */
		private final PersistenceManager<Binary> delegate                 ;
		private final StorageRequestAcceptor     connectionRequestAcceptor;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final PersistenceManager<Binary> delegate                 ,
			final StorageRequestAcceptor     connectionRequestAcceptor
		)
		{
			super();
			this.delegate                  = notNull(delegate)                 ;
			this.connectionRequestAcceptor = notNull(connectionRequestAcceptor);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

//		@Override
//		public final void cleanUp()
//		{
//			this.delegate.cleanUp();
//		}
//
//		@Override
//		public final Object lookupObject(final long oid)
//		{
//			return this.delegate.lookupObject(oid);
//		}
//
//		@Override
//		public final long lookupObjectId(final Object object)
//		{
//			return this.delegate.lookupObjectId(object);
//		}
//
//		@Override
//		public final long ensureObjectId(final Object object)
//		{
//			return this.delegate.ensureObjectId(object);
//		}
//
//		@Override
//		public long currentObjectId()
//		{
//			return this.delegate.currentObjectId();
//		}
//
//		@Override
//		public final PersistenceLoader<Binary> createLoader()
//		{
//			return this.delegate.createLoader();
//		}
//
//		@Override
//		public final PersistenceStorer<Binary> createStorer()
//		{
//			return this.delegate.createStorer();
//		}
//
//		@Override
//		public final PersistenceStorer<Binary> createStorer(final BufferSizeProvider bufferSizeProvider)
//		{
//			return this.delegate.createStorer(bufferSizeProvider);
//		}
//
//		@Override
//		public final PersistenceRegisterer createRegisterer()
//		{
//			return this.delegate.createRegisterer();
//		}
//
//		@Override
//		public final Object initialGet()
//		{
//			return this.delegate.initialGet();
//		}
//
//		@Override
//		public final Object get(final long oid)
//		{
//			return this.delegate.get(oid);
//		}
//
//		@Override
//		public final <C extends Procedure<Object>> C collect(final C collector, final long... oids)
//		{
//			return this.delegate.collect(collector, oids);
//		}
//
//		@Override
//		public final long storeFull(final Object instance)
//		{
//			return this.delegate.storeFull(instance);
//		}
//
//		@Override
//		public long storeRequired(final Object instance)
//		{
//			return this.delegate.storeRequired(instance);
//		}
//
//		@Override
//		public final long[] storeAllFull(final Object... instances)
//		{
//			return this.delegate.storeAllFull(instances);
//		}
//
//		@Override
//		public long[] storeAllRequired(final Object... instances)
//		{
//			return this.delegate.storeAllRequired(instances);
//		}
//
//		@Override
//		public final PersistenceSource<Binary> source()
//		{
//			return this.delegate.source();
//		}
//
//		@Override
//		public void updateMetadata(
//			final PersistenceTypeDictionary typeDictionary ,
//			final long                      highestTypeId  ,
//			final long                      highestObjectId
//		)
//		{
//			this.delegate.updateMetadata(typeDictionary, highestTypeId, highestObjectId);
//		}
//
//		@Override
//		public void updateCurrentObjectId(final long currentObjectId)
//		{
//			this.delegate.updateCurrentObjectId(currentObjectId);
//		}

		@Override
		public PersistenceManager<Binary> persistenceManager()
		{
			return this.delegate;
		}

		@Override
		public final boolean issueGarbageCollection(final long nanoTimeBudgetBound)
		{
			try
			{
				// a time budget <= 0 will effectively be a cheap query for the completion state.
				return this.connectionRequestAcceptor.issueGarbageCollection(nanoTimeBudgetBound);
			}
			catch(final InterruptedException e)
			{
				// thread interrupted, task aborted, return
				return false;
			}
		}

		@Override
		public final boolean issueFileCheck(
			final long                               nanoTimeBudgetBound,
			final StorageDataFileDissolvingEvaluator fileDissolver
		)
		{
			try
			{
				// a time budget <= 0 will effectively be a cheap query for the completion state.
				return this.connectionRequestAcceptor.issueFileCheck(nanoTimeBudgetBound, fileDissolver);
			}
			catch(final InterruptedException e)
			{
				// thread interrupted, task aborted, return
				return false;
			}
		}

		@Override
		public final boolean issueCacheCheck(
			final long                        nanoTimeBudgetBound,
			final StorageEntityCacheEvaluator entityEvaluator
		)
		{
			try
			{
				// a time budget <= 0 will effectively be a cheap query for the completion state.
				return this.connectionRequestAcceptor.issueCacheCheck(nanoTimeBudgetBound, entityEvaluator);
			}
			catch(final InterruptedException e)
			{
				// thread interrupted, task aborted, return
				return false;
			}
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
		public void exportChannels(final StorageIoHandler fileHandler, final boolean performGarbageCollection)
		{
			try
			{
				this.connectionRequestAcceptor.exportChannels(fileHandler, performGarbageCollection);
			}
			catch(final InterruptedException e)
			{
				// thread interrupted, task aborted, return
				return;
			}
		}

		@Override
		public StorageEntityTypeExportStatistics exportTypes(
			final StorageEntityTypeExportFileProvider            exportFileProvider,
			final Predicate<? super StorageEntityTypeHandler<?>> isExportType
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
		public void importFiles(final XGettingEnum<File> importFiles)
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

	}

}
