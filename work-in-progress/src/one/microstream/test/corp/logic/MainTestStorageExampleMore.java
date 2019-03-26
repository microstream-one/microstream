package one.microstream.test.corp.logic;

import java.io.File;

import one.microstream.reference.Reference;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.Storage;
import one.microstream.test.corp.model.ClientCorporation;

public class MainTestStorageExampleMore
{
	// root of the application's data model graph
	static final Reference<ClientCorporation> ROOT          = Reference.New(null)       ;
	static final File                         DIRECTORY     = new File("C:/StorageTest");
	static final int                          CHANNEl_COUNT = 4                         ;

	// create a storage manager, link the root, start the "embedded" database
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.Foundation(
			DIRECTORY, // location for the database files
			Storage.ConfigurationBuilder()
			.setChannelCountProvider  (Storage.ChannelCountProvider(CHANNEl_COUNT))     // storage channel/thread count (default 1)
			.setHousekeepingController(Storage.HousekeepingController(100, 10_000_000)) // time configuration for housekeeping, caching, etc.
			.setEntityCacheEvaluator  (Storage.EntityCacheEvaluator(10_000))            // evalutator for removing entities from the cache
		)
		
		// with registered refactorings
//		.setRefactoringMappingProvider(
//			Persistence.RefactoringMapping(new File("D:/Refactorings.csv"))
//		)
		
		// root resolver with refactorings
//		.setRootResolver(
//		//	Storage.RootResolver(ROOT, Persistence.RefactoringMapping(new File("D:/Refactorings.csv")))
//			PersistenceRootResolver.Builder()
//			.registerRoot("root", ROOT)
//		//	.registerRoots(PersistenceRootResolver.deriveRoots(SomeClassWithConstants.class))
//			.setRefactoring(Persistence.RefactoringMapping(new File("D:/Refactorings.csv")))
//			.build()
//		)
		
		.start(ROOT) // bind graph's root, start DB management threads and return a reference to the manager instance
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
