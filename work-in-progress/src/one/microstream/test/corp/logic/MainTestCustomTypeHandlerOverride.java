package one.microstream.test.corp.logic;

import java.util.ArrayList;

import one.microstream.collections.old.OldCollections;
import one.microstream.persistence.binary.internal.BinaryHandlerArrayList;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;


public class MainTestCustomTypeHandlerOverride
{
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.Foundation()
		.onConnectionFoundation(f ->
		{
			f.getCustomTypeHandlerRegistry()
			.registerTypeHandler(
				new BinaryHandlerArrayList(f.getSizedArrayLengthController()).initializeTypeId(10043)
			);
		})
//		.setRefactoringMappingProvider(
//			Persistence.RefactoringMapping(new File("Refactorings.csv"))
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
			Test.print("TEST: done." );
		}
		
		System.exit(0);
	}
	
	static ArrayList<String> generateGraph()
	{
		return OldCollections.ArrayList("A", "B", "C");
	}
		
}
