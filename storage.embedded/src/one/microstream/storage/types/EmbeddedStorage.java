package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.io.File;

import one.microstream.files.XFiles;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.persistence.types.PersistenceTypeDictionaryIoHandler;
import one.microstream.persistence.types.PersistenceTypeEvaluator;

/**
 * Static utility class containing static pseudo-constructor methods (indicated by a capital first letter)
 * and various utility methods to setup and start a database.
 * <p>
 * In the simplest case, the following call is enough to setup and start an embedded object graph database:<br>
 * {@code EmbeddedStorageManager storage = EmbeddedStorage.start();}<br>
 * Anything beyond that is optimization and customization. As it should be.
 * 
 * @author TM
 */
public final class EmbeddedStorage
{
	/**
	 * Creates an instance of an {@link EmbeddedStorageFoundation} default implementation without any assembly parts set.
	 * 
	 * @return a new {@link EmbeddedStorageFoundation} instance.
	 */
	public static final EmbeddedStorageFoundation<?> createFoundation()
	{
		return EmbeddedStorageFoundation.New();
	}
		
	/**
	 * Pseudo-constructor method to create a new {@link EmbeddedStorageConnectionFoundation} instance
	 * using the passed {@link PersistenceTypeDictionaryIoHandler} and default method references provided by {@link Persistence}.
	 * <p>
	 * Calls {@link #ConnectionFoundation(PersistenceTypeDictionaryIoHandler, PersistenceTypeEvaluator, PersistenceTypeEvaluator)}
	 * with {@link Persistence#isPersistable(Class)} and {@link Persistence#isTypeIdMappable(Class)} method references
	 * as the other two parameters.
	 * <p>
	 * For explanations and customizing values, see {@link #ConnectionFoundation(PersistenceTypeDictionaryIoHandler, PersistenceTypeEvaluator, PersistenceTypeEvaluator)}.
	 * 
	 * @param typeDictionaryIoHandler {@linkDoc EmbeddedStorage#ConnectionFoundation(PersistenceTypeDictionaryIoHandler, PersistenceTypeEvaluator, PersistenceTypeEvaluator):}
	 * 
	 * @return {@linkDoc EmbeddedStorage#ConnectionFoundation(PersistenceTypeDictionaryIoHandler, PersistenceTypeEvaluator, PersistenceTypeEvaluator)@return}
	 * 
	 * @see #ConnectionFoundation(File)
	 * @see #ConnectionFoundation(PersistenceTypeDictionaryIoHandler, PersistenceTypeEvaluator, PersistenceTypeEvaluator)
	 * @see Persistence
	 */
	public static final EmbeddedStorageConnectionFoundation<?> ConnectionFoundation(
		final PersistenceTypeDictionaryIoHandler typeDictionaryIoHandler
	)
	{
		return ConnectionFoundation(
			typeDictionaryIoHandler      ,
			Persistence::isPersistable   ,
			Persistence::isTypeIdMappable
		);
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link EmbeddedStorageConnectionFoundation} instance
	 * using the passed {@literal directory} and default method references provided by {@link Persistence}.
	 * <p>
	 * Calls {@link #ConnectionFoundation(PersistenceTypeDictionaryIoHandler)} with a
	 * {@link PersistenceTypeDictionaryIoHandler} instance constructed from the passed {@literal directory}.
	 * 
	 * @param directory the directory where the {@link PersistenceTypeDictionary} information will be stored.
	 * 
	 * @return {@linkDoc EmbeddedStorage#ConnectionFoundation(PersistenceTypeDictionaryIoHandler)@return}
	 * 
	 * @see PersistenceTypeDictionaryFileHandler#NewInDirectory(File)
	 * @see #ConnectionFoundation(PersistenceTypeDictionaryIoHandler)
	 * @see #ConnectionFoundation(PersistenceTypeDictionaryIoHandler, PersistenceTypeEvaluator, PersistenceTypeEvaluator)
	 * @see Persistence
	 */
	public static final EmbeddedStorageConnectionFoundation<?> ConnectionFoundation(
		final File directory
	)
	{
		return ConnectionFoundation(
			PersistenceTypeDictionaryFileHandler.NewInDirectory(directory)
		);
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link EmbeddedStorageConnectionFoundation} instance
	 * using the passed logic instances as its essential parts.
	 * <p>
	 * 
	 * @param typeDictionaryIoHandler a logic instance to handle a type dictionary's IO operations.
	 * @param typeEvaluatorPersistable evaluator function to determine if instances of a type are persistable.
	 * @param typeEvaluatorTypeIdMappable evaluator function to determine if a type may be encountered
	 *        by the type analysis at all. See {@link}
	 * 
	 * @return a new {@link EmbeddedStorageConnectionFoundation} instance.
	 * 
	 * @see #ConnectionFoundation(PersistenceTypeDictionaryIoHandler)
	 * @see #ConnectionFoundation(File)
	 */
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

	// (04.06.2019 TM)FIXME: /!\ JavaDoc W.i.P.


	
	/**
	 * Returns the default storage directory in the current working directory and with a filename defined by
	 * {@link StorageFileProvider.Defaults#defaultStorageDirectory}.
	 * 
	 * @return the default storage directory located in the current working directory.
	 */
	public static File defaultStorageDirectory()
	{
		return new File(StorageFileProvider.Defaults.defaultStorageDirectory());
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link EmbeddedStorageFoundation} instance
	 * using {@link #defaultStorageDirectory()} as its storage directory and a {@link StorageConfiguration}
	 * instance using defaults.
	 * <p>
	 * Calls {@link #ConnectionFoundation(File)} with {@link #defaultStorageDirectory()}.
	 * 
	 * @return a new all-default {@link EmbeddedStorageFoundation} instance.
	 * 
	 * @see #Foundation(File)
	 * @see #Foundation(StorageConfiguration)
	 * @see #Foundation(StorageConfiguration.Builder)
	 * @see #Foundation(File, StorageConfiguration.Builder)
	 * @see #Foundation(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageFoundation<?> Foundation()
	{
		return Foundation(EmbeddedStorage.defaultStorageDirectory());
	}
	
	
	/**
	 * Pseudo-constructor method to create a new {@link EmbeddedStorageFoundation} instance
	 * using the passed {@literal directory} and a defaults for the remaining configuration.
	 * 
	 * @param directory {@linkDoc EmbeddedStorage#Foundation(File, StorageConfiguration.Builder):}
	 * 
	 * @return a new {@link EmbeddedStorageFoundation} instance using the passed storage directory.
	 * 
	 * @see #Foundation()
	 * @see #Foundation(StorageConfiguration)
	 * @see #Foundation(StorageConfiguration.Builder)
	 * @see #Foundation(File, StorageConfiguration.Builder)
	 * @see #Foundation(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
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
	
	/**
	 * Pseudo-constructor method to create a new {@link EmbeddedStorageFoundation} instance
	 * using the passed {@link StorageConfiguration.Builder}.
	 * <p>
	 * The {@link #defaultStorageDirectory()} will be set as the builder's storage directory and a
	 * {@link StorageConfiguration} will be created from it.
	 * 
	 * @param configuration {@linkDoc EmbeddedStorage#Foundation(File, StorageConfiguration.Builder):}
	 * 
	 * @return {@linkDoc EmbeddedStorage#Foundation(File, StorageConfiguration.Builder)@return}
	 * 
	 * @see #Foundation()
	 * @see #Foundation(File)
	 * @see #Foundation(StorageConfiguration.Builder)
	 * @see #Foundation(File, StorageConfiguration.Builder)
	 * @see #Foundation(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageFoundation<?> Foundation(
		final StorageConfiguration.Builder<?> configuration
	)
	{
		return Foundation(
			EmbeddedStorage.defaultStorageDirectory(),
			configuration
		);
	}
	
	/**
	 * Pseudo-constructor method to create a new {@link EmbeddedStorageFoundation} instance
	 * using the passed {@literal directory} and {@link StorageConfiguration.Builder}.
	 * <p>
	 * A new {@link StorageFileProvider} is created for the passed {@literal directory} and set to the passed
	 * {@link StorageConfiguration.Builder}, which provides a {@link StorageConfiguration} to be passed to
	 * {@link #Foundation(StorageConfiguration)}.
	 * 
	 * @param directory the directory where the storage will be located.
	 * @param configuration the {@link StorageConfiguration.Builder} to be used.
	 * 
	 * @return a new {@link EmbeddedStorageFoundation} instance using the passed configuration.
	 * 
	 * @see #Foundation()
	 * @see #Foundation(File)
	 * @see #Foundation(StorageConfiguration)
	 * @see #Foundation(StorageConfiguration.Builder)
	 * @see #Foundation(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
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
	
	/**
	 * Pseudo-constructor method to create a new {@link EmbeddedStorageFoundation} instance
	 * using the passed {@link StorageConfiguration}.
	 * 
	 * @param configuration the {@link StorageConfiguration} to be used.
	 * 
	 * @return a new {@link EmbeddedStorageFoundation} instance using the passed configuration.
	 * 
	 * @see #Foundation()
	 * @see #Foundation(File)
	 * @see #Foundation(StorageConfiguration.Builder)
	 * @see #Foundation(File, StorageConfiguration.Builder)
	 * @see #Foundation(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
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
		
	/**
	 * Pseudo-constructor method to create a new {@link EmbeddedStorageFoundation} instance
	 * using the passed {@link StorageConfiguration} and {@link EmbeddedStorageConnectionFoundation}.
	 * 
	 * @param configuration the {@link StorageConfiguration} to be used.
	 * 
	 * @param connectionFoundation the {@link EmbeddedStorageConnectionFoundation} instance to be used for creating new
	 *        connections, i.e. context for storing and loading data.
	 * 
	 * @return a new {@link EmbeddedStorageFoundation} instance using the passed configuration.
	 * 
	 * @see #Foundation()
	 * @see #Foundation(File)
	 * @see #Foundation(StorageConfiguration)
	 * @see #Foundation(StorageConfiguration.Builder)
	 * @see #Foundation(File, StorageConfiguration.Builder)
	 */
	public static final EmbeddedStorageFoundation<?> Foundation(
		final StorageConfiguration                   configuration       ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		return createFoundation()
			.setConfiguration(configuration)
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

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private EmbeddedStorage()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
