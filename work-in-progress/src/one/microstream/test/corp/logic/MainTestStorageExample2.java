package one.microstream.test.corp.logic;

import one.microstream.collections.types.XGettingTable;
import one.microstream.concurrency.XThreads;
import one.microstream.hashing.HashStatistics;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;


public class MainTestStorageExample2
{
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
//		.Foundation()
//		.setLockFileSetupProvider(Storage.LockFileSetupProvider())
//		.setRefactoringMappingProvider(
//			Persistence.RefactoringMapping(new File("D:/Refactorings.csv"))
//		)
//		.onConnectionFoundation(e ->
//			e.setRefactoringMappingProvider(
//				Persistence.RefactoringMapping(new File("D:/Refactorings.csv"))
//			)
//		)
		.start()
	;

	public static void main(final String[] args)
	{
		// either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root().get() == null)
		{
			// first execution enters here

			Test.print("TEST: model data required." );
			STORAGE.root().set(Test.generateModelData(1_000));
			Test.print("STORAGE: storing ...");
			STORAGE.store(STORAGE.root());
//			STORAGE.issueFullFileCheck();
			Test.print("STORAGE: storing completed.");
//			printObjectRegistryStatistics();
		}
		else
		{
			// subsequent executions enter here

			Test.print("TEST: model data loaded." );
			Test.print(STORAGE.root().get());
			Test.print("TEST: exporting data ..." );
//			printObjectRegistryStatistics();
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testCorpExport"));
			Test.print("TEST: data export completed.");
		}
		
//		STORAGE.shutdown();
		XThreads.sleep(2000);
		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
	
	static void printObjectRegistryStatistics()
	{
		final XGettingTable<String, ? extends HashStatistics> stats =
			STORAGE.persistenceManager().objectRegistry().createHashStatistics()
		;
		stats.iterate(e ->
		{
			System.out.println(e.key());
			System.out.println(e.value());
		});
	}
	
}
