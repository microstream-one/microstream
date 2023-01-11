package one.microstream.storage.embedded.types;

/*-
 * #%L
 * microstream-storage-embedded
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

import java.io.File;
import java.nio.file.Path;

import one.microstream.afs.nio.types.NioFileSystem;
import one.microstream.afs.types.ADirectory;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.persistence.types.PersistenceTypeDictionaryIoHandler;
import one.microstream.persistence.types.PersistenceTypeEvaluator;
import one.microstream.storage.types.Storage;
import one.microstream.storage.types.StorageBackupSetup;
import one.microstream.storage.types.StorageConfiguration;
import one.microstream.storage.types.StorageLiveFileProvider;


/**
 * Static utility class containing static pseudo-constructor methods (indicated by a capital first letter)
 * and various utility methods to set up and start a database.
 * <p>
 * In the simplest case, the following call is enough to set up and start an embedded object graph database:<br>
 * {@code EmbeddedStorageManager storage = EmbeddedStorage.start();}<br>
 * Anything beyond that is optimization and customization. As it should be.
 *
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
	 * @param typeDictionaryIoHandler a logic instance to handle a type dictionary's IO operations.
	 *
	 * @return a new {@link EmbeddedStorageConnectionFoundation} instance.
	 *
	 * @see #ConnectionFoundation(ADirectory)
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

	public static final EmbeddedStorageConnectionFoundation<?> ConnectionFoundation(
		final StorageConfiguration     configuration           ,
		final PersistenceTypeEvaluator typeEvaluatorPersistable
	)
	{
		final StorageBackupSetup backupSetup = configuration.backupSetup();
		final PersistenceTypeDictionaryIoHandler btdih = backupSetup != null
			? backupSetup.backupFileProvider().provideTypeDictionaryIoHandler()
			: null
		;

		final StorageLiveFileProvider fileProvider = configuration.fileProvider();
		final PersistenceTypeDictionaryIoHandler tdih = fileProvider.provideTypeDictionaryIoHandler(btdih);

		return ConnectionFoundation(tdih, typeEvaluatorPersistable);
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
	 * @return a new {@link EmbeddedStorageConnectionFoundation} instance.
	 *
	 * @see #ConnectionFoundation(PersistenceTypeDictionaryIoHandler)
	 * @see #ConnectionFoundation(PersistenceTypeDictionaryIoHandler, PersistenceTypeEvaluator)
	 * @see Persistence
	 */
	public static final EmbeddedStorageConnectionFoundation<?> ConnectionFoundation(
		final ADirectory directory
	)
	{
		return ConnectionFoundation(
			PersistenceTypeDictionaryFileHandler.New(directory)
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
	 * @see #ConnectionFoundation(ADirectory)
	 */
	public static final EmbeddedStorageConnectionFoundation<?> ConnectionFoundation(
		final PersistenceTypeDictionaryIoHandler typeDictionaryIoHandler ,
		final PersistenceTypeEvaluator           typeEvaluatorPersistable
	)
	{
		return EmbeddedStorageConnectionFoundation.New()
			.setTypeDictionaryIoHandler (typeDictionaryIoHandler)
			.setTypeEvaluatorPersistable(typeEvaluatorPersistable)
		;
	}


	/**
	 * Pseudo-constructor method to create a new {@link EmbeddedStorageFoundation} instance
	 * using {@link Storage#defaultStorageDirectory()} as its storage directory and default values for
	 * its {@link StorageConfiguration}.
	 * <p>
	 * Calls {@link #ConnectionFoundation(ADirectory)} with {@link Storage#defaultStorageDirectory()}.
	 *
	 * @return a new all-default {@link EmbeddedStorageFoundation} instance.
	 *
	 * @see #Foundation(ADirectory)
	 * @see #Foundation(StorageConfiguration)
	 * @see #Foundation(StorageConfiguration.Builder)
	 * @see #Foundation(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageFoundation<?> Foundation()
	{
		return Foundation(Storage.defaultStorageDirectory());
	}

	/**
	 * @param directory the storage directory
	 * @return a new storage foundation
	 * 
	 * @deprecated replaced by {@link #Foundation(Path)}, will be removed in version 8
	 */
	@Deprecated
	public static final EmbeddedStorageFoundation<?> Foundation(
		final File directory
	)
	{
		return Foundation(directory.toPath());
	}
	
	public static final EmbeddedStorageFoundation<?> Foundation(
		final Path directory
	)
	{
		// no directory ensuring required since the file provider does that internally

		return Foundation(
			NioFileSystem.New(directory.getFileSystem())
				.ensureDirectory(directory)
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link EmbeddedStorageFoundation} instance
	 * using the passed {@literal directory} and default values for the remaining parts of
	 * its {@link StorageConfiguration}.
	 * <p>
	 * Calls {@link #Foundation(StorageConfiguration)} with a newly created
	 * {@link StorageConfiguration} using the passed directory as its storage location.
	 *
	 * @param directory the directory where the storage will be located.
	 *
	 * @return a new {@link EmbeddedStorageFoundation} instance using the passed storage directory.
	 *
	 * @see #Foundation()
	 * @see #Foundation(StorageConfiguration)
	 * @see #Foundation(StorageConfiguration.Builder)
	 * @see #Foundation(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageFoundation<?> Foundation(
		final ADirectory directory
	)
	{
		// no directory ensuring required since the file provider does that internally

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
	 * @see #Foundation(ADirectory)
	 * @see #Foundation(StorageConfiguration.Builder)
	 * @see #Foundation(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageFoundation<?> Foundation(
		final StorageConfiguration configuration
	)
	{
		return createFoundation()
			.setConfiguration(configuration)
		;
	}

	/**
	 * Pseudo-constructor method to create a new {@link EmbeddedStorageFoundation} instance
	 * using the passed {@link StorageConfiguration.Builder} to build its {@link StorageConfiguration}.
	 * <p>
	 * This is merely a convenience alias for {@code Foundation(configuration.createConfiguration());}
	 * 
	 * @param configuration the {@link StorageConfiguration.Builder} to be used.
	 *
	 * @return a new {@link EmbeddedStorageFoundation} instance using the passed directory and configuration.
	 *
	 * @see #Foundation()
	 * @see #Foundation(Path)
	 * @see #Foundation(StorageConfiguration)
	 * @see #Foundation(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageFoundation<?> Foundation(
		final StorageConfiguration.Builder<?> configuration
	)
	{
		return Foundation(
			configuration.createConfiguration()
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
	 * @see #Foundation(ADirectory)
	 * @see #Foundation(StorageConfiguration)
	 * @see #Foundation(StorageConfiguration.Builder)
	 */
	public static final EmbeddedStorageFoundation<?> Foundation(
		final StorageConfiguration                   configuration       ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		return Foundation(configuration)
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
	 * @see #start(ADirectory)
	 * @see #start(StorageConfiguration)
	 * @see #start(StorageConfiguration.Builder)
	 * @see #start(Object, one.microstream.storage.types.StorageConfiguration.Builder)
	 * @see #start(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageManager start()
	{
		return start((Object)null);
	}

	/**
	 * @param directory the directory where the storage will be located.
	 *
	 * @return an {@link EmbeddedStorageManager} instance connected to an actively running database.
	 * 
	 * @deprecated replaced by {@link #start(Path)}, will be removed in version 8
	 */
	@Deprecated
	public static final EmbeddedStorageManager start(
		final File directory
	)
	{
		return start(directory.toPath());
	}

	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * the passed {@literal directory} as its storage location and defaults for the remaining values.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 *
	 * @param directory the directory where the storage will be located.
	 *
	 * @return an {@link EmbeddedStorageManager} instance connected to an actively running database.
	 *
	 * @see #start()
	 * @see #start(StorageConfiguration)
	 * @see #start(StorageConfiguration.Builder)
	 * @see #start(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageManager start(
		final Path directory
	)
	{
		return start(null, directory);
	}
	
	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * the passed {@literal directory} as its storage location and defaults for the remaining values.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 *
	 * @param directory the directory where the storage will be located.
	 *
	 * @return an {@link EmbeddedStorageManager} instance connected to an actively running database.
	 *
	 * @see #start()
	 * @see #start(StorageConfiguration)
	 * @see #start(StorageConfiguration.Builder)
	 * @see #start(StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageManager start(
		final ADirectory directory
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
	 * @return an {@link EmbeddedStorageManager} instance connected to an actively running database.
	 *
	 * @see #start()
	 * @see #start(ADirectory)
	 * @see #start(StorageConfiguration.Builder)
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
	 * @return an {@link EmbeddedStorageManager} instance connected to an actively running database.
	 *
	 * @see #start()
	 * @see #start(ADirectory)
	 * @see #start(StorageConfiguration)
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
	 * the passed {@link StorageConfiguration} and {@link EmbeddedStorageConnectionFoundation}.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 *
	 * @param configuration        the {@link StorageConfiguration} to be used.
	 *
	 * @param connectionFoundation the {@link EmbeddedStorageConnectionFoundation} to be used instead of a
	 *        generically created one.
	 *
	 * @return an {@link EmbeddedStorageManager} instance connected to an actively running database.
	 *
	 * @see #start()
	 * @see #start(ADirectory)
	 * @see #start(StorageConfiguration)
	 * @see #start(StorageConfiguration.Builder)
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
	 * and defaults for the remaining values.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 *
	 * @param root the explicitly defined root instance of the persistent entity graph.
	 *
	 * @return an {@link EmbeddedStorageManager} instance connected to an actively running database.
	 *
	 * @see #start(Object, ADirectory)
	 * @see #start(Object, StorageConfiguration)
	 * @see #start(Object, StorageConfiguration.Builder)
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
	 * @param root      root the explicitly defined root instance of the persistent entity graph.
	 *
	 * @param directory the directory where the storage will be located.
	 *
	 * @return an {@link EmbeddedStorageManager} instance connected to an actively running database.
	 * 
	 * @deprecated replaced by {@link #start(Object, Path)} and {@link #start(Object, ADirectory)}, will be removed in version 8
	 */
	@Deprecated
	public static final EmbeddedStorageManager start(
		final Object root     ,
		final File   directory
	)
	{
		return start(root, directory.toPath());
	}

	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * the passed {@literal root} as the persistent entity graph's root instance,
	 * the passed {@literal directory} as its storage location and defaults for the remaining values.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 *
	 * @param root      root the explicitly defined root instance of the persistent entity graph.
	 *
	 * @param directory the directory where the storage will be located.
	 *
	 * @return an {@link EmbeddedStorageManager} instance connected to an actively running database.
	 *
	 * @see #start(Object)
	 * @see #start(Object, StorageConfiguration)
	 * @see #start(Object, StorageConfiguration.Builder)
	 * @see #start(Object, StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageManager start(
		final Object root     ,
		final Path   directory
	)
	{
		return createAndStartStorageManager(
			Foundation(directory),
			root
		);
	}
	
	/**
	 * Convenience method to configure, create and start a {@link EmbeddedStorageManager} using
	 * the passed {@literal root} as the persistent entity graph's root instance,
	 * the passed {@literal directory} as its storage location and defaults for the remaining values.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 *
	 * @param root      root the explicitly defined root instance of the persistent entity graph.
	 *
	 * @param directory the directory where the storage will be located.
	 *
	 * @return an {@link EmbeddedStorageManager} instance connected to an actively running database.
	 *
	 * @see #start(Object)
	 * @see #start(Object, StorageConfiguration)
	 * @see #start(Object, StorageConfiguration.Builder)
	 * @see #start(Object, StorageConfiguration, EmbeddedStorageConnectionFoundation)
	 */
	public static final EmbeddedStorageManager start(
		final Object     root     ,
		final ADirectory directory
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
	 * @param root          root the explicitly defined root instance of the persistent entity graph.
	 *
	 * @param configuration the {@link StorageConfiguration} to be used.
	 *
	 * @return an {@link EmbeddedStorageManager} instance connected to an actively running database.
	 *
	 * @see #start(Object)
	 * @see #start(Object, ADirectory)
	 * @see #start(Object, StorageConfiguration.Builder)
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
	 * @param root          root the explicitly defined root instance of the persistent entity graph.
	 *
	 * @param configuration the {@link StorageConfiguration.Builder} to be used.
	 *
	 * @return an {@link EmbeddedStorageManager} instance connected to an actively running database.
	 *
	 * @see #start(Object)
	 * @see #start(Object, ADirectory)
	 * @see #start(Object, StorageConfiguration)
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
	 * the passed {@literal root} as the persistent entity graph's root instance,
	 * {@link StorageConfiguration} and {@link EmbeddedStorageConnectionFoundation}.
	 * <p>
	 * See {@link #Foundation()} variants for more practical/configurable alternatives.
	 *
	 * @param root                 root the explicitly defined root instance of the persistent entity graph.
	 *
	 * @param configuration        the {@link StorageConfiguration} to be used.
	 *
	 * @param connectionFoundation the {@link EmbeddedStorageConnectionFoundation} to be used instead of a
	 *        generically created one.
	 *
	 * @return an {@link EmbeddedStorageManager} instance connected to an actively running database.
	 *
	 * @see #start(Object)
	 * @see #start(Object, ADirectory)
	 * @see #start(Object, StorageConfiguration)
	 * @see #start(Object, StorageConfiguration.Builder)
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
	 * @param root       the persistent entity graph's root instance, potentially null.
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
	 * @throws UnsupportedOperationException when called
	 */
	private EmbeddedStorage()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
