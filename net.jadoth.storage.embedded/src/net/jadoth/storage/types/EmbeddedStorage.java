package net.jadoth.storage.types;

import java.io.File;

import net.jadoth.X;
import net.jadoth.collections.Singleton;
import net.jadoth.files.XFiles;
import net.jadoth.persistence.internal.CompositeSwizzleIdProvider;
import net.jadoth.persistence.internal.FileObjectIdProvider;
import net.jadoth.persistence.internal.FileTypeIdProvider;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.reference.Reference;

public final class EmbeddedStorage
{
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
	public static final Reference<Object> root()
	{
		return root;
	}
	
	
	
	public static final EmbeddedStorageFoundation createFoundationBlank()
	{
		return new EmbeddedStorageFoundation.Implementation();
	}
	
	public static final EmbeddedStorageConnectionFoundation createConnectionFoundation(final File directory)
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

		return new EmbeddedStorageConnectionFoundation.Implementation()
			.setDictionaryStorage          (dictionaryStorage            )
			.setSwizzleIdProvider          (idProvider                   )
			.setTypeEvaluatorPersistable   (Persistence::isPersistable   )
			.setTypeEvaluatorTypeIdMappable(Persistence::isTypeIdMappable)
		;
	}

	
	
	public static final EmbeddedStorageFoundation createFoundation(
		final StorageConfiguration                configuration       ,
		final EmbeddedStorageConnectionFoundation connectionFoundation
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
		final StorageFileProvider                 fileProvider        ,
		final EmbeddedStorageConnectionFoundation connectionFoundation
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
		XFiles.ensureDirectory(directory);

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
		final File                          directory   ,
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
		final StorageConfiguration                configuration       ,
		final EmbeddedStorageConnectionFoundation connectionFoundation
	)
	{
		final EmbeddedStorageManager esm = createFoundation(configuration, connectionFoundation)
			.createEmbeddedStorageManager()
		;
		esm.start();
		
		return esm;
	}
	
	public static final EmbeddedStorageManager start(
		final StorageFileProvider                 fileProvider        ,
		final EmbeddedStorageConnectionFoundation connectionFoundation
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
		final EmbeddedStorageManager esm = createFoundation(directory)
			.createEmbeddedStorageManager()
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
		final EmbeddedStorageManager esm = createFoundation()
			.createEmbeddedStorageManager()
		;
		esm.start();
		
		return esm;
	}
	
	public static final EmbeddedStorageManager start(
		final File                          directory   ,
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
			.createEmbeddedStorageManager()
		;
		esm.start();
		
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
