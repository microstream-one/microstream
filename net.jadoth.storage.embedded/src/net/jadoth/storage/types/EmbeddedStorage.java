package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import java.io.File;

import net.jadoth.files.XFiles;
import net.jadoth.persistence.internal.CompositeSwizzleIdProvider;
import net.jadoth.persistence.internal.FileObjectIdProvider;
import net.jadoth.persistence.internal.FileTypeIdProvider;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
import net.jadoth.persistence.types.Persistence;

public final class EmbeddedStorage
{
	public static final EmbeddedStorageFoundation createFoundationBlank()
	{
		return new EmbeddedStorageFoundation.Implementation();
	}
	
	public static final EmbeddedStorageConnectionFoundation<?> createConnectionFoundation(
		final File directory
	)
	{
		/*
		 * (03.11.2014)TODO: EmbeddedStorage loosely coupled id providers?
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

		final CompositeSwizzleIdProvider idProvider = new CompositeSwizzleIdProvider(fileTypeIdProvider, fileObjectIdProvider)
			.initialize()
		;

		return EmbeddedStorageConnectionFoundation.New()
			.setTypeDictionaryStorage      (dictionaryStorage            )
			.setSwizzleIdProvider          (idProvider                   )
			.setTypeEvaluatorPersistable   (Persistence::isPersistable   )
			.setTypeEvaluatorTypeIdMappable(Persistence::isTypeIdMappable)
		;
	}

	
	
	public static final EmbeddedStorageFoundation createFoundation(
		final StorageConfiguration                   configuration       ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		/* (24.09.2018 TM)NOTE:
		 * Configuration and ConnectionFoundation both depend on a File (directory)
		 * So this is the most elementary creator method possible.
		 */
		return createFoundationBlank()
			.setConfiguration(configuration)
			.setConnectionFoundation(connectionFoundation)
		;
	}
	
	public static final EmbeddedStorageFoundation createFoundation(
		final StorageFileProvider                    fileProvider        ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation
	)
	{
		return createFoundationBlank()
			.setConfiguration(
				Storage.Configuration(fileProvider)
			)
			.setConnectionFoundation(connectionFoundation)
		;
	}
	
	public static final EmbeddedStorageFoundation createFoundation(final File directory)
	{
		XFiles.ensureDirectory(notNull(directory));

		return createFoundation(
			Storage.FileProvider(directory),
			createConnectionFoundation(directory)
		);
	}

	public static final EmbeddedStorageFoundation createFoundation()
	{
		return createFoundation(new File(Storage.defaultDirectoryName()));
	}
	
	public static final EmbeddedStorageFoundation createFoundation(
		final File                          directory             ,
		final StorageChannelCountProvider   channelCountProvider  ,
		final StorageHousekeepingController housekeepingController,
		final StorageDataFileEvaluator      fileDissolver         ,
		final StorageEntityCacheEvaluator   entityCacheEvaluator
	)
	{
		XFiles.ensureDirectory(directory);

		return createFoundation(
			Storage.Configuration(
				Storage.FileProvider(directory),
				channelCountProvider           ,
				housekeepingController         ,
				fileDissolver                  ,
				entityCacheEvaluator
			),
			createConnectionFoundation(directory)
		);
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
		final EmbeddedStorageManager esm = createFoundation(configuration, connectionFoundation)
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
		final EmbeddedStorageManager esm = createFoundation(fileProvider, connectionFoundation)
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
		final EmbeddedStorageManager esm = createFoundation(directory)
			.createEmbeddedStorageManager(explicitRoot)
		;
		esm.start();
		
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
		return start((Object)null); // no explicit root. Not to be confused with start(File)
	}
	
	public static final EmbeddedStorageManager start(final Object explicitRoot)
	{
		final EmbeddedStorageManager esm = createFoundation()
			.createEmbeddedStorageManager(explicitRoot)
		;
		esm.start();
		
		return esm;
	}
	
	public static final EmbeddedStorageManager start(
		final Object                        explicitRoot          ,
		final File                          directory             ,
		final StorageChannelCountProvider   channelCountProvider  ,
		final StorageHousekeepingController housekeepingController,
		final StorageDataFileEvaluator      fileDissolver         ,
		final StorageEntityCacheEvaluator   entityCacheEvaluator
	)
	{
		final EmbeddedStorageManager esm = createFoundation(
			directory             ,
			channelCountProvider  ,
			housekeepingController,
			fileDissolver         ,
			entityCacheEvaluator
		)
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
