package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.io.File;

import one.microstream.files.XFiles;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceTypeDictionaryIoHandler;
import one.microstream.persistence.types.PersistenceTypeEvaluator;

/**
 * {@link one.microstream.storage.types.EmbeddedStorage}
 * @author TM
 *
 */
public final class EmbeddedStorage
{
	/**
	 * Creates an instance of an {@link EmbeddedStorageFoundation} implementation without any assembly parts set.
	 * 
	 * @return
	 */
	public static final EmbeddedStorageFoundation<?> createFoundation()
	{
		return EmbeddedStorageFoundation.New();
	}
	
	public static final EmbeddedStorageConnectionFoundation<?> ConnectionFoundation(
		final PersistenceTypeDictionaryIoHandler typeDictionaryIoHandler
	)
	{
		return ConnectionFoundation(
			typeDictionaryIoHandler,
			Persistence::isPersistable     ,
			Persistence::isTypeIdMappable
		);
	}
	
	public static final EmbeddedStorageConnectionFoundation<?> ConnectionFoundation(
		final PersistenceTypeDictionaryIoHandler typeDictionaryIoHandler    ,
		final PersistenceTypeEvaluator           typeEvaluatorPersistable   ,
		final PersistenceTypeEvaluator           typeEvaluatorTypeIdMappable
	)
	{
		return EmbeddedStorageConnectionFoundation.New()
			.setTypeDictionaryIoHandler    (typeDictionaryIoHandler)
			.setTypeEvaluatorPersistable   (typeEvaluatorPersistable)
			.setTypeEvaluatorTypeIdMappable(typeEvaluatorTypeIdMappable)
		;
	}
	
	public static final EmbeddedStorageConnectionFoundation<?> ConnectionFoundation(
		final File directory
	)
	{
		return ConnectionFoundation(
			PersistenceTypeDictionaryFileHandler.NewInDirectory(directory)
		);
	}



	
	public static File defaultStorageDirectory()
	{
		return new File(StorageFileProvider.Defaults.defaultStorageDirectory());
	}
	
	public static final EmbeddedStorageFoundation<?> Foundation()
	{
		return Foundation(EmbeddedStorage.defaultStorageDirectory());
	}
	
	public static final EmbeddedStorageFoundation<?> Foundation(
		final StorageConfiguration.Builder<?> configuration
	)
	{
		return Foundation(
			EmbeddedStorage.defaultStorageDirectory(),
			configuration
		);
	}
	
	public static final EmbeddedStorageFoundation<?> Foundation(
		final File directory
	)
	{
		XFiles.ensureDirectory(notNull(directory));

		return Foundation(
			Storage.Configuration(
				Storage.FileProvider(directory)
			)
		);
	}
	
	public static final EmbeddedStorageFoundation<?> Foundation(
		final File                            directory    ,
		final StorageConfiguration.Builder<?> configuration
	)
	{
		XFiles.ensureDirectory(directory);
		final StorageFileProvider fileProvider = Storage.FileProvider(directory);

		return Foundation(
			configuration
			.setStorageFileProvider(fileProvider)
			.createConfiguration()
		);
	}
	
	public static final EmbeddedStorageFoundation<?> Foundation(
		final StorageConfiguration configuration
	)
	{
		final StorageBackupSetup backupSetup = configuration.backupSetup();
		final PersistenceTypeDictionaryIoHandler btdih = backupSetup != null
			? backupSetup.backupFileProvider().provideTypeDictionaryIoHandler()
			: null
		;

		final StorageFileProvider fileProvider = configuration.fileProvider();
		final PersistenceTypeDictionaryIoHandler tdih = fileProvider.provideTypeDictionaryIoHandler(btdih);
		
		return Foundation(
			configuration,
			ConnectionFoundation(tdih)
		);
	}
		
	public static final EmbeddedStorageFoundation<?> Foundation(
		final StorageConfiguration                   storageConfiguration,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		return createFoundation()
			.setConfiguration(storageConfiguration)
			.setConnectionFoundation(connectionFoundation)
		;
	}
			
	public static final EmbeddedStorageManager start(
		final StorageConfiguration                   configuration       ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		return start(null, configuration, connectionFoundation);
	}
	

	public static final EmbeddedStorageManager start(
		final Object                                 explicitRoot        ,
		final StorageConfiguration                   configuration       ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		final EmbeddedStorageManager esm = Foundation(configuration, connectionFoundation)
			.createEmbeddedStorageManager(explicitRoot)
		;
		esm.start();
		
		return esm;
	}
	
	public static final EmbeddedStorageManager start(
		final StorageFileProvider                    fileProvider        ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		return start(null, fileProvider, connectionFoundation);
	}
	
	public static final EmbeddedStorageManager start(
		final Object                                 explicitRoot        ,
		final StorageFileProvider                    fileProvider        ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		final EmbeddedStorageManager esm = Foundation(Storage.Configuration(fileProvider), connectionFoundation)
			.createEmbeddedStorageManager()
		;
		esm.start();
		
		return esm;
	}
	
	public static final EmbeddedStorageManager start(final File directory)
	{
		return start(null, directory);
	}
	
	public static final EmbeddedStorageManager start(final Object explicitRoot, final File directory)
	{
		final EmbeddedStorageManager esm = Foundation(directory)
			.createEmbeddedStorageManager(explicitRoot)
		;
		esm.start();
		
		return esm;
	}

	/**
	 * Uber-simplicity util method. See {@link #ensureStorageManager()} and {@link #Foundation()} variants for
	 * more practical alternatives.
	 * 
	 * @return An {@link EmbeddedStorageManager} instance with an actively running database using all-default-settings.
	 */
	public static final EmbeddedStorageManager start()
	{
		return start((Object)null); // no explicit root. Not to be confused with start(File)
	}
	
	public static final EmbeddedStorageManager start(final Object explicitRoot)
	{
		final EmbeddedStorageManager esm = Foundation()
			.createEmbeddedStorageManager(explicitRoot)
		;
		esm.start();
		
		return esm;
	}
	
	public static final EmbeddedStorageManager start(
		final Object                          explicitRoot ,
		final File                            directory    ,
		final StorageConfiguration.Builder<?> configuration
	)
	{
		final EmbeddedStorageManager esm = Foundation(directory, configuration)
		.createEmbeddedStorageManager(explicitRoot).start();
		
		return esm;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private EmbeddedStorage()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
