package one.microstream.test.corp.main;

import one.microstream.collections.types.XGettingTable;
import one.microstream.hashing.HashStatistics;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.Storage;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStorageExample2
{
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.Foundation(
			Storage.ConfigurationBuilder()
			.setDataFileEvaluator(Storage.DataFileEvaluator(1_000, 10_000, 1.0f))
		)
//		.setLockFileSetupProvider(Storage.LockFileSetupProvider())
//		.setRefactoringMappingProvider(
//			Persistence.RefactoringMapping(new File("D:/Refactorings.csv"))
//		)
//		.onConnectionFoundation(e ->
//			e.setRefactoringMappingProvider(
//				Persistence.RefactoringMapping(new File("D:/Refactorings.csv"))
//			)
//		)

//		.setLockFileSetupProvider(Storage.LockFileSetupProvider(30_000))

		.start()
	;

	public static void main(final String[] args)
	{
		// either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.defaultRoot().get() == null)
		{
			// first execution enters here

			Test.print("TEST: model data required.");
			STORAGE.defaultRoot().set(Test.generateModelData(1_000));
			Test.print("STORAGE: storing ...");
			STORAGE.storeDefaultRoot();
//			STORAGE.issueFullFileCheck();
			Test.print("STORAGE: storing completed.");
//			printObjectRegistryStatistics();
		}
		else
		{
			// subsequent executions enter here

			Test.print("TEST: model data loaded." );
			Test.print(STORAGE.defaultRoot().get());
			Test.print("TEST: exporting data ..." );
//			printObjectRegistryStatistics();
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testCorpExport"));
			Test.print("TEST: data export completed.");
		}
		
//		STORAGE.shutdown();
//		XThreads.sleep(2000);
//		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
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
