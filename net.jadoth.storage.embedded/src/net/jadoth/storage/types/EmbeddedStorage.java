package net.jadoth.storage.types;

import java.io.File;

import net.jadoth.persistence.internal.FileObjectIdProvider;
import net.jadoth.persistence.internal.FilePersistenceTypeDictionary;
import net.jadoth.persistence.internal.FileSwizzleIdProvider;
import net.jadoth.persistence.internal.FileTypeIdProvider;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceRootResolver;
import net.jadoth.util.file.JadothFiles;

public final class EmbeddedStorage
{
	public static EmbeddedStorageFoundation createFoundation()
	{
		return new EmbeddedStorageFoundation.Implementation();
	}

	public static EmbeddedStorageFoundation createFoundation(final StorageConfiguration configuration)
	{
		System.out.println("EmbeddedStorage#createFoundation using storage configuration:\n" + configuration);

		return createFoundation().setConfiguration(configuration);
	}

	public static EmbeddedStorageFoundation createFoundation(
		final StorageConfiguration                configuration        ,
		final EmbeddedStorageConnectionFoundation connectionFoundation
	)
	{
		return createFoundation(configuration)
			.setConnectionFoundation(connectionFoundation)
		;
	}

	public static EmbeddedStorageFoundation createFoundation(
		final StorageConfiguration                configuration        ,
		final EmbeddedStorageConnectionFoundation connectionFoundation,
		final PersistenceRootResolver             rootResolver
	)
	{
		return createFoundation(configuration, connectionFoundation)
			.setRootResolver(rootResolver)
		;
	}

	public static EmbeddedStorageFoundation createFoundation(final StorageFileProvider fileProvider)
	{
		return createFoundation(
			Storage.Configuration(
				fileProvider,
				Storage.ChannelCountProvider(),
				Storage.HousekeepingController(),
				Storage.DataFileEvaluator(),
				Storage.EntityCacheEvaluator()
			)
		);
	}

	public static EmbeddedStorageFoundation createFoundation(
		final StorageFileProvider                 fileProvider        ,
		final EmbeddedStorageConnectionFoundation connectionFoundation
	)
	{
		return createFoundation(fileProvider)
			.setConnectionFoundation(connectionFoundation)
		;
	}

	public static EmbeddedStorageFoundation createFoundation(
		final StorageFileProvider                 fileProvider        ,
		final EmbeddedStorageConnectionFoundation connectionFoundation,
		final PersistenceRootResolver             rootResolver
	)
	{
		return createFoundation(fileProvider, connectionFoundation)
			.setRootResolver(rootResolver)
		;
	}
	
	public static EmbeddedStorageFoundation createFoundation(final File directory)
	{
		JadothFiles.ensureDirectory(directory);

		return createFoundation(
			Storage.FileProvider(directory),
			createConnectionFoundation(directory)
		);
	}

	public static EmbeddedStorageFoundation createFoundation(
		final File                    directory   ,
		final PersistenceRootResolver rootResolver
	)
	{
		JadothFiles.ensureDirectory(directory);

		return createFoundation(
			Storage.FileProvider(directory),
			createConnectionFoundation(directory),
			rootResolver
		);
	}

	public static EmbeddedStorageFoundation createFoundation(
		final PersistenceRootResolver       rootResolver,
		final File                          directory   ,
		final StorageChannelCountProvider   channelCountProvider  ,
		final StorageHousekeepingController housekeepingController,
		final StorageDataFileEvaluator      fileDissolver         ,
		final StorageEntityCacheEvaluator   entityCacheEvaluator
	)
	{
		JadothFiles.ensureDirectory(directory);

		return createFoundation(
			Storage.Configuration(
				Storage.FileProvider(directory),
				channelCountProvider           ,
				housekeepingController         ,
				fileDissolver                  ,
				entityCacheEvaluator
			),
			createConnectionFoundation(directory),
			rootResolver
		);
	}

	public static EmbeddedStorageFoundation createFoundation(final PersistenceRootResolver rootResolver)
	{
		return createFoundation(new File(Storage.defaultDirectoryName()), rootResolver);
	}


	static EmbeddedStorageConnectionFoundation createConnectionFoundation(final File directory)
	{
		/* (03.11.2014)TODO: EmbeddedStorage loosely coupled id providers?
		 * shouldn't the providers below be somehow loosely coupled?
		 * There also has to be an opportunity to configure things like id range increment
		 */
		final FilePersistenceTypeDictionary dictionaryStorage = new FilePersistenceTypeDictionary(
			new File(directory, Persistence.defaultFilenameTypeDictionary())
		);

		final FileTypeIdProvider fileTypeIdProvider = new FileTypeIdProvider(
			new File(directory, Persistence.defaultFilenameTypeId())
		);

		final FileObjectIdProvider fileObjectIdProvider = new FileObjectIdProvider(
			new File(directory, Persistence.defaultFilenameObjectId())
		);

		final FileSwizzleIdProvider idProvider = new FileSwizzleIdProvider(fileTypeIdProvider, fileObjectIdProvider)
			.initialize()
		;

		return new EmbeddedStorageConnectionFoundation.Implementation()
			.setDictionaryStorage          (dictionaryStorage            )
			.setSwizzleIdProvider          (idProvider                   )
			.setTypeEvaluatorPersistable   (Persistence::isPersistable   )
			.setTypeEvaluatorTypeIdMappable(Persistence::isTypeIdMappable)
		;
	}


	public static final EmbeddedStorageManager createStorageManager(
		final PersistenceRootResolver rootResolver
	)
	{
		final EmbeddedStorageManager esm = EmbeddedStorage
			.createFoundation(rootResolver)
			.createEmbeddedStorageManager()
		;
		return esm;
	}

	public static final EmbeddedStorageManager createStorageManager()
	{
		final EmbeddedStorageManager esm = EmbeddedStorage
			.createFoundation()
			.createEmbeddedStorageManager()
		;
		return esm;
	}

	public static final EmbeddedStorageManager createStorageManager(
		final PersistenceRootResolver rootResolver,
		final File                    directory
	)
	{
		final EmbeddedStorageManager esm = EmbeddedStorage
			.createFoundation(directory, rootResolver)
			.createEmbeddedStorageManager()
		;
		return esm;
	}

	public static final EmbeddedStorageManager createStorageManager(
		final PersistenceRootResolver rootResolver,
		final StorageFileProvider     fileProvider
	)
	{
		final EmbeddedStorageManager esm = EmbeddedStorage
			.createFoundation(fileProvider)
			.setRootResolver(rootResolver)
			.createEmbeddedStorageManager()
		;
		return esm;
	}

	public static final EmbeddedStorageManager createStorageManager(
		final PersistenceRootResolver rootResolver ,
		final StorageConfiguration    configuration
	)
	{
		final EmbeddedStorageManager esm = EmbeddedStorage
			.createFoundation(configuration)
			.setRootResolver(rootResolver)
			.createEmbeddedStorageManager()
		;
		return esm;
	}
	
	/**
	 * Uber-simplicity util method. See {@link #createStorageManager()} and {@link #createFoundation()} variants for
	 * more practical alternatives.
	 * 
	 * @return An {@link EmbeddedStorageManager} instance with an actively running database using all-default-settings.
	 */
	public static final EmbeddedStorageManager start()
	{
		final EmbeddedStorageManager esm = createStorageManager();
		esm.start();
		return esm;
	}

	
	
	/**
	 * Default root reference to point to the root instance of a persistent entity graph.
	 * This is moreless a monkey business because proper applications should not rely on static state as their
	 * entity graph root but define their own with a proper typing and a suitable identifier.
	 * The only reasonable thing about this variable is that is lowers the learning curve as it eliminates the
	 * need to explicitely define a root resolver.
	 */
	static Object root;
	
	/**
	 * The default instance to be used as a root of the persistence entity graph.<br>
	 * The value is <code>null</code> until an actual instance is set via {@link #root(Object)}.<br>
	 * 
	 * @return the default root instance.
	 * 
	 * @see #root(Object)
	 */
	public static Object root()
	{
		return root;
	}
	
	/**
	 * Sets the default root instance to be returned by {@link #root()}.<br>
	 * Returns the reference that was references as root so far.
	 * 
	 * @param rootInstance the new root instance to be set.
	 * @return the old root reference.
	 * 
	 * @see #root()
	 */
	public static Object root(final Object rootInstance)
	{
		final Object oldRoot = root;
		root = rootInstance;
		return oldRoot;
	}
	


	private EmbeddedStorage()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
