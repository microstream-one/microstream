package net.jadoth.storage.types;

import java.io.File;
import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.collections.Singleton;
import net.jadoth.persistence.internal.FileObjectIdProvider;
import net.jadoth.persistence.internal.FilePersistenceTypeDictionary;
import net.jadoth.persistence.internal.FileSwizzleIdProvider;
import net.jadoth.persistence.internal.FileTypeIdProvider;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceRootResolver;
import net.jadoth.reference.Reference;
import net.jadoth.util.file.JadothFiles;

public final class EmbeddedStorage
{
	public static EmbeddedStorageFoundation createFoundationBlank()
	{
		return new EmbeddedStorageFoundation.Implementation();
	}

	public static EmbeddedStorageFoundation createFoundation(final StorageConfiguration configuration)
	{
		return createFoundationBlank().setConfiguration(configuration);
	}
	
	public static EmbeddedStorageFoundation createFoundation()
	{
		return createFoundation(new File(Storage.defaultDirectoryName()));
	}
	
	public static EmbeddedStorageFoundation createFoundation(
		final Consumer<? super EmbeddedStorageFoundation> customLogic
	)
	{
		final EmbeddedStorageFoundation esf = createFoundation();
		customLogic.accept(esf);
		return esf;
	}
	
	public static EmbeddedStorageFoundation createFoundation(final PersistenceRootResolver rootResolver)
	{
		return createFoundation()
			.setRootResolver(rootResolver)
		;
	}

	public static EmbeddedStorageFoundation createFoundation(
		final StorageConfiguration                configuration       ,
		final EmbeddedStorageConnectionFoundation connectionFoundation
	)
	{
		return createFoundation(configuration)
			.setConnectionFoundation(connectionFoundation)
		;
	}
	
	

	public static EmbeddedStorageFoundation createFoundation(
		final StorageConfiguration                configuration       ,
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
	 * Default root instance of a persistent entity graph.
	 * This is moreless a monkey business because proper applications should not rely on static state as their
	 * entity graph root but define their own root with a proper type parameter and a suitable identifier.
	 * The only reason for this thing's existence is that it lowers the learning curve as it eliminates the
	 * need to explicitely define and register a root resolver.
	 */
	static final Singleton<Object> root = X.Singleton(null);
	
	/**
	 * The default instance to be used as a root of the persistence entity graph.<br>
	 * The reference value is initially <code>null</code>.<br>
	 * 
	 * @return the default root instance.
	 * 
	 * @see #root(Object)
	 */
	public static Reference<Object> root()
	{
		return root;
	}
		


	private EmbeddedStorage()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
