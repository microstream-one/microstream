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
	 * Calls {@link #ConnectionFoundation(PersistenceTypeDictionaryIoHandler, PersistenceTypeEvaluator)}
	 * with {@link Persistence#isPersistable(Class)} method references as the other parameter.
	 * <p>
	 * For explanations and customizing values, see {@link #ConnectionFoundation(PersistenceTypeDictionaryIoHandler, PersistenceTypeEvaluator)}.
	 * 
	 * @param typeDictionaryIoHandler {@linkDoc EmbeddedStorage#ConnectionFoundation(PersistenceTypeDictionaryIoHandler, PersistenceTypeEvaluator):}
	 * 
	 * @return {@linkDoc EmbeddedStorage#ConnectionFoundation(PersistenceTypeDictionaryIoHandler, PersistenceTypeEvaluator)@return}
	 * 
	 * @see #ConnectionFoundation(File)
	 * @see #ConnectionFoundation(PersistenceTypeDictionaryIoHandler, PersistenceTypeEvaluator)
	 * @see Persistence
	 */
	public static final EmbeddedStorageConnectionFoundation<?> ConnectionFoundation(
		final PersistenceTypeDictionaryIoHandler typeDictionaryIoHandler
	)
	{
		return ConnectionFoundation(
			typeDictionaryIoHandler   ,
			Persistence::isPersistable
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
	 * @see #ConnectionFoundation(PersistenceTypeDictionaryIoHandler, PersistenceTypeEvaluator)
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
	 * @param typeDictionaryIoHandler     a logic instance to handle a type dictionary's IO operations.
	 * 
	 * @param typeEvaluatorPersistable    evaluator function to determine if instances of a type are persistable.
	 * 
	 * @return a new {@link EmbeddedStorageConnectionFoundation} instance.
	 * 
	 * @see #ConnectionFoundation(PersistenceTypeDictionaryIoHandler)
	 * @see #ConnectionFoundation(File)
	 */
	public static final EmbeddedStorageConnectionFoundation<?> ConnectionFoundation(
		final PersistenceTypeDictionaryIoHandler typeDictionaryIoHandler ,
		final PersistenceTypeEvaluator           typeEvaluatorPersistable
	)
	{
		return EmbeddedStorageConnectionFoundation.New()
			.setTypeDictionaryIoHandler    (typeDictionaryIoHandler)
			.setTypeEvaluatorPersistable   (typeEvaluatorPersistable)
		;
	}
	
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
	 * using {@link #defaultStorageDirectory()} as its storage directory and default values for
	 * its {@link StorageConfiguration}.
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
	 * using the passed {@literal directory} and default values for the remaining parts of
	 * its {@link StorageConfiguration}.
	 * <p>
	 * Calls {@link #Foundation(StorageConfiguration)} with a newly created
	 * {@link StorageConfiguration} using the passed directory as its storage location.
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
	 * using the passed {@link StorageConfiguration.Builder} to build its {@link StorageConfiguration}.
	 * <p>
	 * The {@link #defaultStorageDirectory()} will be set as the builder's storage directory, after which
	 * a {@link StorageConfiguration} will be created from it.
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
	 * using the passed {@literal directory} and {@link StorageConfiguration.Builder} to build its
	 * {@link StorageConfiguration}.
	 * <p>
	 * A new {@link StorageFileProvider} is created from the passed {@literal directory} and default values,
	 * then set to the passed {@link StorageConfiguration.Builder}, which then provides a {@link StorageConfiguration}
	 * to be passed to {@link #Foundation(StorageConfiguration)}.
	 * 
	 * @param directory     the directory where the storage will be located.
	 * 
	 * @param configuration the {@link StorageConfiguration.Builder} to be used.
	 * 
	 * @return a new {@link EmbeddedStorageFoundation} instance using the passed directory and configuration.
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
	 * using the passed {@link StorageConfiguration} and {@link EmbeddedStorageConnectionFoundation}.
	 * 
	 * @param configuration        the {@link StorageConfiguration} to be used.
	 * 
	 * @param connectionFoundation the {@link EmbeddedStorageConnectionFoundation} instance to be used for creating new
	 *        connections, i.e. the context for issuing commands and storing and loading data.
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
	
	
	
	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * purely default values.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 * 
	 * @return an {@link EmbeddedStorageManager} instance connected to an actively running database.
	 * 
	 * @see #start(File)
	 * @see #start(StorageConfiguration)
	 * @see #start(StorageConfiguration.Builder)
	 * @see #start(File, StorageConfiguration.Builder)
	 * @see #start(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageManager start()
	{
		return start((Object)null);
	}
	
	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * the passed {@literal directory} as its storage location and defaults for the remainings values.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 * 
	 * @param directory the directory where the storage will be located.
	 * 
	 * @return {@linkDoc EmbeddedStorage#start()@return}
	 * 
	 * @see #start()
	 * @see #start(StorageConfiguration)
	 * @see #start(StorageConfiguration.Builder)
	 * @see #start(File, StorageConfiguration.Builder)
	 * @see #start(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageManager start(
		final File directory
	)
	{
		return start(null, directory);
	}
	
	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * the passed {@link StorageConfiguration}.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 * 
	 * @param configuration the {@link StorageConfiguration} to be used.
	 * 
	 * @return {@linkDoc EmbeddedStorage#start()@return}
	 * 
	 * @see #start()
	 * @see #start(File)
	 * @see #start(StorageConfiguration.Builder)
	 * @see #start(File, StorageConfiguration.Builder)
	 * @see #start(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageManager start(
		final StorageConfiguration configuration
	)
	{
		return start(null, configuration);
	}
	
	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * the passed {@link StorageConfiguration.Builder} to build its {@link StorageConfiguration}.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 * 
	 * @param configuration the {@link StorageConfiguration.Builder} to be used.
	 * 
	 * @return {@linkDoc EmbeddedStorage#start()@return}
	 * 
	 * @see #start()
	 * @see #start(File)
	 * @see #start(StorageConfiguration)
	 * @see #start(File, StorageConfiguration.Builder)
	 * @see #start(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageManager start(
		final StorageConfiguration.Builder<?> configuration
	)
	{
		return start(null, configuration);
	}
	
	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * the passed {@literal directory} and {@link StorageConfiguration.Builder} to build its
	 * {@link StorageConfiguration}.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 * 
	 * @param directory     the directory where the storage will be located.
	 * 
	 * @param configuration the {@link StorageConfiguration.Builder} to be used.
	 * 
	 * @return {@linkDoc EmbeddedStorage#start()@return}
	 * 
	 * @see #start()
	 * @see #start(File)
	 * @see #start(StorageConfiguration)
	 * @see #start(StorageConfiguration.Builder)
	 * @see #start(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageManager start(
		final File                            directory    ,
		final StorageConfiguration.Builder<?> configuration
	)
	{
		return start(null, directory, configuration);
	}
	
	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * the passed {@link StorageConfiguration} and {@link EmbeddedStorageConnectionFoundation}.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 * 
	 * @param configuration        the {@link StorageConfiguration} to be used.
	 * 
	 * @param connectionFoundation the {@link EmbeddedStorageConnectionFoundation} to be used instead of a
	 *        generically created one.
	 * 
	 * @return {@linkDoc EmbeddedStorage#start()@return}
	 * 
	 * @see #start()
	 * @see #start(File)
	 * @see #start(StorageConfiguration)
	 * @see #start(StorageConfiguration.Builder)
	 * @see #start(File, StorageConfiguration.Builder)
	 */
	public static final EmbeddedStorageManager start(
		final StorageConfiguration                   configuration       ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		return start(null, configuration, connectionFoundation);
	}
	
	
	
	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * the passed {@literal root} as the persistent entity graph's root instance
	 * and defaults for the remainings values.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 * 
	 * @param root the explicitely defined root instance of the persistent entity graph.
	 * 
	 * @return {@linkDoc EmbeddedStorage#start()@return}
	 * 
	 * @see #start(Object, File)
	 * @see #start(Object, StorageConfiguration)
	 * @see #start(Object, StorageConfiguration.Builder)
	 * @see #start(Object, File, StorageConfiguration.Builder)
	 * @see #start(Object, StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageManager start(
		final Object root
	)
	{
		return createAndStartStorageManager(
			Foundation(),
			root
		);
	}
	
	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * the passed {@literal root} as the persistent entity graph's root instance,
	 * the passed {@literal directory} as its storage location and defaults for the remainings values.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 * 
	 * @param root      {@linkDoc EmbeddedStorage#start(Object):}
	 * 
	 * @param directory {@linkDoc EmbeddedStorage#start(File):}
	 * 
	 * @return {@linkDoc EmbeddedStorage#start()@return}
	 * 
	 * @see #start(Object)
	 * @see #start(Object, StorageConfiguration)
	 * @see #start(Object, StorageConfiguration.Builder)
	 * @see #start(Object, File, StorageConfiguration.Builder)
	 * @see #start(Object, StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageManager start(
		final Object root     ,
		final File   directory
	)
	{
		return createAndStartStorageManager(
			Foundation(directory),
			root
		);
	}
	
	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * the passed {@literal root} as the persistent entity graph's root instance
	 * and the passed {@link StorageConfiguration}.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 * 
	 * @param root          {@linkDoc EmbeddedStorage#start(Object):}
	 * 
	 * @param configuration {@linkDoc EmbeddedStorage#start(StorageConfiguration):}
	 * 
	 * @return {@linkDoc EmbeddedStorage#start()@return}
	 * 
	 * @see #start(Object)
	 * @see #start(Object, File)
	 * @see #start(Object, StorageConfiguration.Builder)
	 * @see #start(Object, File, StorageConfiguration.Builder)
	 * @see #start(Object, StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageManager start(
		final Object               root         ,
		final StorageConfiguration configuration
	)
	{
		return createAndStartStorageManager(
			Foundation(configuration),
			root
		);
	}
	
	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * the passed {@literal root} as the persistent entity graph's root instance
	 * and the passed {@link StorageConfiguration.Builder} to build its {@link StorageConfiguration}.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 * 
	 * @param root          {@linkDoc EmbeddedStorage#start(Object):}
	 * 
	 * @param configuration {@linkDoc EmbeddedStorage#start(StorageConfiguration.Builder):}
	 * 
	 * @return {@linkDoc EmbeddedStorage#start()@return}
	 * 
	 * @see #start(Object)
	 * @see #start(Object, File)
	 * @see #start(Object, StorageConfiguration)
	 * @see #start(Object, File, StorageConfiguration.Builder)
	 * @see #start(Object, StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageManager start(
		final Object                          root         ,
		final StorageConfiguration.Builder<?> configuration
	)
	{
		return createAndStartStorageManager(
			Foundation(configuration),
			root
		);
	}
		
	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * the passed {@literal root} as the persistent entity graph's root instance and
	 * the passed {@literal directory} and {@link StorageConfiguration.Builder} to build its
	 * {@link StorageConfiguration}.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 * 
	 * @param root          {@linkDoc EmbeddedStorage#start(Object):}
	 * 
	 * @param directory     {@linkDoc EmbeddedStorage#start(File):}
	 * 
	 * @param configuration {@linkDoc EmbeddedStorage#start(StorageConfiguration.Builder):}
	 * 
	 * @return {@linkDoc EmbeddedStorage#start()@return}
	 * 
	 * @see #start(Object)
	 * @see #start(Object, File)
	 * @see #start(Object, StorageConfiguration)
	 * @see #start(Object, StorageConfiguration.Builder)
	 * @see #start(Object, StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageManager start(
		final Object                          root         ,
		final File                            directory    ,
		final StorageConfiguration.Builder<?> configuration
	)
	{
		return createAndStartStorageManager(
			Foundation(directory, configuration),
			root
		);
	}
	
	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * the passed {@literal root} as the persistent entity graph's root instance,
	 * {@link StorageConfiguration} and {@link EmbeddedStorageConnectionFoundation}.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 * 
	 * @param root                 {@linkDoc EmbeddedStorage#start(Object):}
	 * 
	 * @param configuration        {@linkDoc EmbeddedStorage#start(StorageConfiguration, EmbeddedStorageConnectionFoundation):}
	 * 
	 * @param connectionFoundation {@linkDoc EmbeddedStorage#start(StorageConfiguration, EmbeddedStorageConnectionFoundation):}
	 * 
	 * @return {@linkDoc EmbeddedStorage#start()@return}
	 * 
	 * @see #start(Object)
	 * @see #start(Object, File)
	 * @see #start(Object, StorageConfiguration)
	 * @see #start(Object, StorageConfiguration.Builder)
	 * @see #start(Object, File, StorageConfiguration.Builder)
	 */
	public static final EmbeddedStorageManager start(
		final Object                                 root                ,
		final StorageConfiguration                   configuration       ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		return createAndStartStorageManager(
			Foundation(configuration, connectionFoundation),
			root
		);
	}
	
	/**
	 * Utility method to encapsulate the code to create and start an {@link EmbeddedStorageManager}.
	 * 
	 * @param foundation the {@link EmbeddedStorageFoundation} to be used.
	 * 
	 * @param root       the persistent entiy graph's root instance, potentially null.
	 * 
	 * @return a newly created and started {@link EmbeddedStorageManager} instance.
	 */
	private static final EmbeddedStorageManager createAndStartStorageManager(
		final EmbeddedStorageFoundation<?> foundation,
		final Object                       root
	)
	{
		final EmbeddedStorageManager esm = foundation
			.createEmbeddedStorageManager(root)
		;
		esm.start();
		
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
