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

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import one.microstream.afs.nio.types.NioFileSystem;
import one.microstream.afs.types.ADirectory;
import one.microstream.afs.types.AFileSystem;
import one.microstream.persistence.types.Persistence;


/**
 * Static utility class containing static pseudo-constructor methods (indicated by a capital first letter)
 * and various utility methods.
 * <p>
 * To setup and start a database, see the class "EmbeddedStorage".
 *
 *
 * 
 * @see Persistence
 * @see StorageChannel
 */
public final class Storage
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	/**
	 * Trivial helper constant.
	 */
	private static final long ONE_MILLION = 1_000_000L;

	

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	static final long millisecondsToNanoseconds(final long milliseconds)
	{
		return milliseconds * ONE_MILLION;
	}

	static final long nanosecondsToMilliseconds(final long nanoseconds)
	{
		return nanoseconds / ONE_MILLION;
	}

	static final long millisecondsToSeconds(final long milliseconds)
	{
		return milliseconds / ONE_MILLION;
	}


	
	public static NioFileSystem DefaultFileSystem()
	{
		return NioFileSystem.New();
	}
	
	public static ADirectory defaultStorageDirectory()
	{
		return defaultStorageDirectory(DefaultFileSystem());
	}
	
	/**
	 * Returns the default storage directory in the current working directory and with a filename defined by
	 * {@link StorageLiveFileProvider.Defaults#defaultStorageDirectory}.
	 *
	 * @return the default storage directory located in the current working directory.
	 */
	public static ADirectory defaultStorageDirectory(final AFileSystem fileSystem)
	{
		return fileSystem.ensureRoot(StorageLiveFileProvider.Defaults.defaultStorageDirectory());
	}
	
	/**
	 * Creates a new {@link StorageLiveFileProvider}.
	 * <p>
	 * For a detailed explanation see {@link StorageLiveFileProvider#New()}.
	 *
	 * @see Storage#FileProvider(Path)
	 * @see StorageLiveFileProvider#New()
	 * @see StorageLiveFileProvider.Builder
	 * @see StorageLiveFileProvider.Defaults
	 */
	public static final StorageLiveFileProvider FileProvider()
	{
		return StorageLiveFileProvider.New();
	}

	/**
	 * Alias for {@code FileProvider(storageDirectory.toPath())}
	 *
	 * @deprecated replaced by {@link #FileProvider(Path)}
	 */
	@Deprecated
	public static final StorageLiveFileProvider FileProvider(final File storageDirectory)
	{
		return FileProvider(storageDirectory.toPath());
	}

	/**
	 * Creates a new {@link StorageLiveFileProvider}.
	 * <p>
	 * For a detailed explanation see {@link StorageLiveFileProvider#New(ADirectory)}.
	 *
	 * @see Storage#FileProvider()
	 * @see StorageLiveFileProvider#New(ADirectory)
	 * @see StorageLiveFileProvider.Builder
	 * @see StorageLiveFileProvider.Defaults
	 */
	public static final StorageLiveFileProvider FileProvider(final Path storageDirectory)
	{
		final NioFileSystem nfs = NioFileSystem.New(storageDirectory.getFileSystem());
		
		final ADirectory dir = nfs.ensureDirectory(storageDirectory);
		
		return StorageLiveFileProvider.New(dir);
	}
	
	public static final StorageLiveFileProvider FileProvider(final ADirectory storageDirectory)
	{
		return StorageLiveFileProvider.New(storageDirectory);
	}

	/**
	 * Creates a new {@link StorageLiveFileProvider.Builder}.
	 * <p>
	 * For a detailed explanation see {@link StorageLiveFileProvider#Builder()}.
	 *
	 * @see Storage#FileProvider()
	 * @see Storage#FileProvider(Path)
	 * @see StorageLiveFileProvider.Builder
	 */
	public static final StorageLiveFileProvider.Builder<?> FileProviderBuilder()
	{
		return StorageLiveFileProvider.Builder();
	}
	
	/**
	 * Creates a new {@link StorageLiveFileProvider.Builder}.
	 * <p>
	 * For a detailed explanation see {@link StorageLiveFileProvider#Builder(AFileSystem)}.
	 *
	 * @see Storage#FileProvider()
	 * @see Storage#FileProvider(Path)
	 * @see StorageLiveFileProvider.Builder
	 */
	public static final StorageLiveFileProvider.Builder<?> FileProviderBuilder(final AFileSystem fileSystem)
	{
		return StorageLiveFileProvider.Builder(fileSystem);
	}
	
	
	
	
	public static final StorageBackupFileProvider BackupFileProvider()
	{
		return StorageBackupFileProvider.New();
	}

	@Deprecated
	public static final StorageBackupFileProvider BackupFileProvider(final File storageDirectory)
	{
		return BackupFileProvider(storageDirectory.toPath());
	}

	public static final StorageBackupFileProvider BackupFileProvider(final Path storageDirectory)
	{
		// note that the backup's file system may potentially be completely different from the live file system.
		final NioFileSystem nfs = NioFileSystem.New(storageDirectory.getFileSystem());
		final ADirectory    dir = nfs.resolveDirectory(storageDirectory);
		
		return StorageBackupFileProvider.New(dir);
	}
	
	public static final StorageBackupFileProvider BackupFileProvider(final ADirectory storageDirectory)
	{
		return StorageBackupFileProvider.New(storageDirectory);
	}

	public static final StorageBackupFileProvider.Builder<?> BackupFileProviderBuilder()
	{
		return StorageBackupFileProvider.Builder();
	}
	
	public static final StorageBackupFileProvider.Builder<?> BackupFileProviderBuilder(final AFileSystem fileSystem)
	{
		return StorageBackupFileProvider.Builder(fileSystem);
	}
	
	
	
	

	/**
	 * Creates a new {@link StorageConfiguration}.
	 * <p>
	 * For a detailed explanation see {@link StorageConfiguration#New()}.
	 *
	 * @see Storage#Configuration(StorageLiveFileProvider)
	 * @see StorageConfiguration#New()
	 * @see StorageConfiguration.Builder
	 */
	public static final StorageConfiguration Configuration()
	{
		return StorageConfiguration.New();
	}

	/**
	 * Creates a new {@link StorageConfiguration}.
	 * <p>
	 * For a detailed explanation see {@link StorageConfiguration#New(StorageLiveFileProvider)}.
	 *
	 * @see Storage#Configuration()
	 * @see StorageConfiguration#New(StorageLiveFileProvider)
	 * @see StorageConfiguration.Builder
	 */
	public static final StorageConfiguration Configuration(
		final StorageLiveFileProvider fileProvider
	)
	{
		return StorageConfiguration.New(fileProvider);
	}

	/**
	 * Creates a new {@link StorageConfiguration.Builder}.
	 * <p>
	 * For a detailed explanation see {@link StorageConfiguration#Builder()}.
	 * 
	 * @see Storage#Configuration()
	 * @see Storage#Configuration(StorageLiveFileProvider)
	 * @see StorageConfiguration#Builder()
	 * @see StorageConfiguration.Builder
	 */
	public static final StorageConfiguration.Builder<?> ConfigurationBuilder()
	{
		return StorageConfiguration.Builder();
	}

	/**
	 * Creates a new {@link StorageHousekeepingController}.
	 * <p>
	 * For a detailed explanation see {@link StorageHousekeepingController#New()}.
	 * 
	 * @see Storage#HousekeepingController(long, long)
	 * @see StorageHousekeepingController#New()
	 * @see StorageHousekeepingController.Defaults
	 */
	public static final StorageHousekeepingController HousekeepingController()
	{
		return StorageHousekeepingController.New();
	}

	/**
	 * Creates a new {@link StorageHousekeepingController}.
	 * <p>
	 * For a detailed explanation see {@link StorageHousekeepingController#New(long, long)}.
	 *
	 * @see Storage#HousekeepingController()
	 * @see StorageHousekeepingController#New(long, long)
	 */
	public static final StorageHousekeepingController HousekeepingController(
		final long housekeepingIntervalMs  ,
		final long housekeepingTimeBudgetNs
	)
	{
		return StorageHousekeepingController.New(housekeepingIntervalMs, housekeepingTimeBudgetNs);
	}

	/**
	 * Creates a new {@link StorageEntityCacheEvaluator}.
	 * <p>
	 * For a detailed explanation see {@link StorageEntityCacheEvaluator#New()}.
	 *
	 * @see Storage#EntityCacheEvaluator(long)
	 * @see Storage#EntityCacheEvaluator(long, long)
	 * @see StorageEntityCacheEvaluator#New()
	 */
	public static final StorageEntityCacheEvaluator EntityCacheEvaluator()
	{
		return StorageEntityCacheEvaluator.New();
	}

	/**
	 * Creates a new {@link StorageEntityCacheEvaluator}.
	 * <p>
	 * For a detailed explanation see {@link StorageEntityCacheEvaluator#New(long)}.
	 *
	 * @see Storage#EntityCacheEvaluator()
	 * @see Storage#EntityCacheEvaluator(long, long)
	 * @see StorageEntityCacheEvaluator#New(long)
	 */
	public static final StorageEntityCacheEvaluator EntityCacheEvaluator(
		final long timeoutMs
	)
	{
		return StorageEntityCacheEvaluator.New(timeoutMs);
	}

	/**
	 * Creates a new {@link StorageEntityCacheEvaluator}.
	 * <p>
	 * For a detailed explanation see {@link StorageEntityCacheEvaluator#New(long, long)}.
	 *
	 * @see Storage#EntityCacheEvaluator()
	 * @see Storage#EntityCacheEvaluator(long)
	 * @see StorageEntityCacheEvaluator#New(long, long)
	 */
	public static final StorageEntityCacheEvaluator EntityCacheEvaluator(
		final long timeoutMs,
		final long threshold
	)
	{
		return StorageEntityCacheEvaluator.New(timeoutMs, threshold);
	}

	/**
	 * Creates a new {@link StorageChannelCountProvider}.
	 * <p>
	 * For a detailed explanation see {@link StorageChannelCountProvider#New()}.
	 *
	 * @see Storage#ChannelCountProvider(int)
	 * @see StorageChannelCountProvider#New()
	 */
	public static final StorageChannelCountProvider ChannelCountProvider()
	{
		return StorageChannelCountProvider.New();
	}

	/**
	 * Creates a new {@link StorageChannelCountProvider}.
	 * <p>
	 * For a detailed explanation see {@link StorageChannelCountProvider#New(int)}.
	 */
	public static final StorageChannelCountProvider ChannelCountProvider(final int channelCount)
	{
		return StorageChannelCountProvider.New(channelCount);
	}

	/**
	 * Creates a new {@link StorageDataFileEvaluator}.
	 * <p>
	 * For a detailed explanation see {@link StorageDataFileEvaluator#New()}.
	 *
	 * @see Storage#DataFileEvaluator(int, int)
	 * @see Storage#DataFileEvaluator(int, int, double)
	 * @see StorageDataFileEvaluator#New()
	 */
	public static final StorageDataFileEvaluator DataFileEvaluator()
	{
		return StorageDataFileEvaluator.New();
	}

	/**
	 * Creates a new {@link StorageDataFileEvaluator}.
	 * <p>
	 * For a detailed explanation see {@link StorageDataFileEvaluator#New(int, int)}.
	 *
	 * @see Storage#DataFileEvaluator()
	 * @see Storage#DataFileEvaluator(int, int, double)
	 * @see StorageDataFileEvaluator#New(int, int)
	 */
	public static final StorageDataFileEvaluator DataFileEvaluator(
		final int fileMinimumSize,
		final int fileMaximumSize
	)
	{
		return StorageDataFileEvaluator.New(fileMinimumSize, fileMaximumSize);
	}

	/**
	 * Creates a new {@link StorageDataFileEvaluator}.
	 * <p>
	 * For a detailed explanation see {@link StorageDataFileEvaluator#New(int, int, double)}.
	 *
	 * @see Storage#DataFileEvaluator()
	 * @see Storage#DataFileEvaluator(int, int)
	 * @see StorageDataFileEvaluator#New(int, int, double)
	 */
	public static final StorageDataFileEvaluator DataFileEvaluator(
		final int    fileMinimumSize,
		final int    fileMaximumSize,
		final double minimumUseRatio
	)
	{
		return StorageDataFileEvaluator.New(fileMinimumSize, fileMaximumSize, minimumUseRatio);
	}

	/**
	 * Creates a new {@link StorageDataFileEvaluator}.
	 * <p>
	 * For a detailed explanation see {@link StorageDataFileEvaluator#New(int, int, double, boolean)}.
	 *
	 * @see Storage#DataFileEvaluator()
	 * @see Storage#DataFileEvaluator(int, int)
	 * @see Storage#DataFileEvaluator(int, int, double)
	 * @see StorageDataFileEvaluator#New(int, int, double, boolean)
	 */
	public static final StorageDataFileEvaluator DataFileEvaluator(
		final int     fileMinimumSize,
		final int     fileMaximumSize,
		final double  minimumUseRatio,
		final boolean cleanUpHeadFile
	)
	{
		return StorageDataFileEvaluator.New(fileMinimumSize, fileMaximumSize, minimumUseRatio, cleanUpHeadFile);
	}

	/**
	 * @deprecated replaced by {@link #BackupSetup(Path)}
	 */
	@Deprecated
	public static final StorageBackupSetup BackupSetup(final File backupDirectory)
	{
		return BackupSetup(backupDirectory.toPath());
	}
	
	/**
	 * Creates a new {@link StorageBackupSetup}.
	 * <p>
	 * For a detailed explanation see {@link StorageBackupSetup#New(ADirectory)}.
	 *
	 * @see StorageBackupSetup#New(StorageBackupFileProvider)
	 * @see StorageBackupHandler
	 */
	public static final StorageBackupSetup BackupSetup(final Path backupDirectory)
	{
		// note that the backup's file system may potentially be completely different from the live file system.
		final NioFileSystem nfs = NioFileSystem.New(backupDirectory.getFileSystem());
		final ADirectory dir = nfs.ensureDirectory(backupDirectory);
		
		return BackupSetup(dir);
	}
	
	/**
	 * Creates a new {@link StorageBackupSetup}.
	 * <p>
	 * For a detailed explanation see {@link StorageBackupSetup#New(ADirectory)}.
	 *
	 * @see StorageBackupSetup#New(ADirectory)
	 * @see StorageBackupHandler
	 */
	public static final StorageBackupSetup BackupSetup(final ADirectory backupDirectory)
	{
		return StorageBackupSetup.New(backupDirectory);
	}

	/**
	 * Creates a new {@link StorageBackupSetup}.
	 * <p>
	 * For a detailed explanation see {@link StorageBackupSetup#New(ADirectory)}.
	 *
	 * @see StorageBackupSetup#New(ADirectory)
	 * @see StorageBackupSetup#New(StorageBackupFileProvider)
	 * @see StorageBackupHandler
	 */
	public static final StorageBackupSetup BackupSetup(final String backupDirectoryPath)
	{
		return BackupSetup(Paths.get(backupDirectoryPath));
	}

	/**
	 * Creates a new {@link StorageBackupSetup}.
	 * <p>
	 * For a detailed explanation see {@link StorageBackupSetup#New(StorageBackupFileProvider)}.
	 *
	 * @see StorageBackupSetup#New(StorageBackupFileProvider)
	 * @see StorageBackupHandler
	 */
	public static final StorageBackupSetup BackupSetup(final StorageBackupFileProvider backupFileProvider)
	{
		return StorageBackupSetup.New(backupFileProvider);
	}

	/**
	 * Creates a new {@link StorageLockFileSetup.Provider}.
	 * <p>
	 * For a detailed explanation see {@link StorageLockFileSetup#Provider()}.
	 *
	 * @see StorageLockFileSetup
	 * @see #LockFileSetupProvider(Charset)
	 * @see #LockFileSetupProvider(long)
	 * @see #LockFileSetupProvider(Charset, long)
	 */
	public static StorageLockFileSetup.Provider LockFileSetupProvider()
	{
		return StorageLockFileSetup.Provider();
	}

	/**
	 * Creates a new {@link StorageLockFileSetup.Provider}.
	 * <p>
	 * For a detailed explanation see {@link StorageLockFileSetup#Provider(Charset)}.
	 *
	 * @see StorageLockFileSetup
	 * @see #LockFileSetupProvider()
	 * @see #LockFileSetupProvider(long)
	 * @see #LockFileSetupProvider(Charset, long)
	 */
	public static StorageLockFileSetup.Provider LockFileSetupProvider(
		final Charset charset
	)
	{
		return StorageLockFileSetup.Provider(charset);
	}

	/**
	 * Creates a new {@link StorageLockFileSetup.Provider}.
	 * <p>
	 * For a detailed explanation see {@link StorageLockFileSetup#Provider(long)}.
	 *
	 * @see StorageLockFileSetup
	 * @see #LockFileSetupProvider()
	 * @see #LockFileSetupProvider(Charset)
	 * @see #LockFileSetupProvider(Charset, long)
	 */
	public static StorageLockFileSetup.Provider LockFileSetupProvider(
		final long updateInterval
	)
	{
		return StorageLockFileSetup.Provider(updateInterval);
	}

	/**
	 * Creates a new {@link StorageLockFileSetup.Provider}.
	 * <p>
	 * For a detailed explanation see {@link StorageLockFileSetup#Provider(Charset, long)}.
	 *
	 * @see StorageLockFileSetup
	 * @see #LockFileSetupProvider()
	 * @see #LockFileSetupProvider(Charset)
	 * @see #LockFileSetupProvider(long)
	 */
	public static StorageLockFileSetup.Provider LockFileSetupProvider(
		final Charset charset       ,
		final long    updateInterval
	)
	{
		return StorageLockFileSetup.Provider(charset, updateInterval);
	}

	/**
	 * Consolidates the storage system represented by the passed {@link StorageConnection} by calling<br>
	 * {@link StorageConnection#issueFullGarbageCollection()}<br>
	 * {@link StorageConnection#issueFullFileCheck()}<br>
	 * {@link StorageConnection#issueFullCacheCheck(StorageEntityCacheEvaluator)}<br>
	 * in that order.
	 * <p>
	 * Depending on the passed functions, this call can do anything from cleaning/optimizing the storage a little to
	 * fully reorganize/optimize the storage files, clear the complete cache and making the storage virtually dormant
	 * until the next store.
	 *
	 * @param storageConnection The connection to the storage that shall be consolidated.
	 * @param entityEvaluator   The function evaluating whether to clear an entity from the cache.<br>
	 *                          May be {@literal null} to indicate the use of the live configuration as a default.
	 *
	 * @return the passed storageConnection instance.
	 */
	public static final <C extends StorageConnection> C consolidate(
		final C                           storageConnection,
		final StorageEntityCacheEvaluator entityEvaluator
	)
	{
		/* calls must be done in that order to achieve highest (normal) effectivity:
		 * - first discard all unreachable entities.
		 * - then cleanup files depending on the resulting data occupation.
		 * - clean the entity cache.
		 */
		storageConnection.issueFullGarbageCollection();
		storageConnection.issueFullFileCheck();
		storageConnection.issueFullCacheCheck(entityEvaluator);
		return storageConnection;
	}

	/**
	 * Calls {@link Storage#consolidate(StorageConnection, StorageEntityCacheEvaluator)}
	 * with {@literal null} as additional parameters (causing live configuration to be used instead).
	 *
	 * @param storageConnection The connection to the storage that shall be consolidated.
	 *
	 * @return the passed storageConnection instance.
	 */
	public static final <C extends StorageConnection> C consolidate(final C storageConnection)
	{
		return consolidate(storageConnection, null);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 *
	 * @throws UnsupportedOperationException
	 */
	private Storage()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
