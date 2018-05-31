package net.jadoth.test.corp.logic;

import java.io.File;

import net.jadoth.persistence.types.Persistence;
import net.jadoth.reference.Reference;
import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.storage.types.Storage;
import net.jadoth.test.corp.model.ClientCorporation;

public class MainTestStorageExampleMore
{
	// root of the application's data model graph
	static final Reference<ClientCorporation> ROOT          = Reference.New(null)       ;
	static final File                         DIRECTORY     = new File("C:/StorageTest");
	static final int                          CHANNEl_COUNT = 4                         ;

	// create a storage manager, link the root, start the "embedded" database
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		
		// with refactorings
//		.createStorageManager(Storage.RootResolver(ROOT, Storage.RefactoringMapping(new File(DIRECTORY, "Refactorings.csv"))))
//		.createStorageManager(
////			Storage.RootResolver(ROOT, Storage.RefactoringMapping(new File("D:\\Refactorings.csv")))
//			Storage.RootResolverBuilder()
//			.registerRoot("root", ROOT)
////			.registerRoots(PersistenceRootResolver.deriveRoots(SomeClassWithConstants.class))
//			.setRefactoring(Storage.RefactoringMapping(new File("D:\\Refactorings.csv")))
//			.build()
//		)
		
		.createStorageManager(
			Persistence.RootResolver(ROOT)                       , // binding between graph's root instance and the storage
			Storage.Configuration(
				Storage.FileProvider(DIRECTORY)                  , // location for the database files
				Storage.ChannelCountProvider(CHANNEl_COUNT)      , // amount of storage channels (parallel database threads)
				Storage.HousekeepingController(1000, 10_000_000) , // housekeeping time config (file cleanup, cache checks, etc.)
				Storage.DataFileEvaluator()                      , // evalutator for dissolving old files
				Storage.EntityCacheEvaluatorCustomTimeout(10_000)  // evalutator for unloading entities from the cache
			)
		)
		.start()
	;
	
	public static void main(final String[] args)
	{
		// either loaded on startup from an existing DB or required to be generated.
		if(ROOT.get() == null)
		{
			// first execution enters here

			Test.print("TEST: model data required." );
			ROOT.set(Test.generateModelData(100_000));

			Test.print("STORAGE: storing ...");
			STORAGE.store(ROOT);
			Test.print("STORAGE: storing completed.");
		}
		else
		{
			// subsequent executions enter here

			Test.print("TEST: model data loaded." );
			Test.print(ROOT.get());
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory(DIRECTORY, "testCorpExport"));
		}

		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
}

class SomeClassWithConstants
{
	public static final int[]    SOME_NUMBERS = {1,2,3};
	public static final String[] SOME_NAMES   = {"Huey", "Dewey", "Louie"};
//	public static final Object[] SOME_STUFFS  = {null, null};
}
