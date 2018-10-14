package net.jadoth.test.corp.logic;

import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageManager;


public class MainTestStorageExample
{
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.createFoundation()
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
			STORAGE.root().set(Test.generateModelData(10_000));
			Test.print("STORAGE: storing ...");
			STORAGE.store(STORAGE.root());
			STORAGE.issueFullFileCheck();
			Test.print("STORAGE: storing completed.");
		}
		else
		{
			// subsequent executions enter here

			Test.print("TEST: model data loaded." );
			Test.print(STORAGE.root().get());
			Test.print("TEST: exporting data ..." );
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testCorpExport"));
			Test.print("TEST: data export completed.");
		}
		
//		STORAGE.shutdown();
		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
}
