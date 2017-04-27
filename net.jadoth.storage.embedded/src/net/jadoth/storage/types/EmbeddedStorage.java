package net.jadoth.storage.types;

import java.io.File;

import net.jadoth.persistence.internal.FileObjectIdProvider;
import net.jadoth.persistence.internal.FileSwizzleIdProvider;
import net.jadoth.persistence.internal.FileTypeIdProvider;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
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
		final PersistenceTypeDictionaryFileHandler dictionaryStorage = PersistenceTypeDictionaryFileHandler.New(
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

	public static final EmbeddedStorageManager createStorageManager(
		final PersistenceRootResolver       rootResolver          ,
		final File                          directory             ,
		final StorageChannelCountProvider   channelCountProvider  ,
		final StorageHousekeepingController housekeepingController,
		final StorageDataFileEvaluator      fileDissolver         ,
		final StorageEntityCacheEvaluator   entityCacheEvaluator
	)
	{
		final EmbeddedStorageManager esm = EmbeddedStorage
			.createFoundation(
				rootResolver          ,
				directory             ,
				channelCountProvider  ,
				housekeepingController,
				fileDissolver         ,
				entityCacheEvaluator
			)
			.createEmbeddedStorageManager()
		;
		return esm;
	}



	private EmbeddedStorage()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
