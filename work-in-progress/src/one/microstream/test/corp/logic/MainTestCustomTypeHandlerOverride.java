package one.microstream.test.corp.logic;

import java.util.ArrayList;

import one.microstream.collections.old.OldCollections;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;


public class MainTestCustomTypeHandlerOverride
{
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
//		.Foundation()
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
		if(STORAGE.root().get() == null)
		{
			Test.print("TEST: graph required." );
			STORAGE.root().set(generateGraph());
			Test.print("STORAGE: storing ...");
			STORAGE.store(STORAGE.root());
			Test.print("STORAGE: storing completed.");
		}
		else
		{
			Test.print("TEST: graph loaded." );
			Test.print(STORAGE.root().get());
			Test.print("TEST: exporting data ..." );
		}
		
		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
	
	static ArrayList<String> generateGraph()
	{
		return OldCollections.ArrayList("A", "B", "C");
	}
		
}
