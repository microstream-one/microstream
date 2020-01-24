package one.microstream.test.corp.main;

import java.util.ArrayList;

import one.microstream.X;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.old.OldCollections;
import one.microstream.io.XIO;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceTypeRegistry;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;


public class MainTestCustomTypeIdMapping
{
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.Foundation()
		.onThis(f ->
		{
			registerCustomTypeIdMappings(f);
		})
		.setRefactoringMappingProvider(
			Persistence.RefactoringMapping(XIO.Path("Refactorings.csv"))
		)
//		.onConnectionFoundation(f ->
//		{
//			f.getCustomTypeHandlerRegistry().registerTypeHandler(new BinaryHandlerHashMapFlattened());
//		})
		.start()
	;
	
	static void registerCustomTypeIdMappings(final EmbeddedStorageFoundation<?> f)
	{
		final EqHashTable<Class<?>, Long> customTypeIds = EqHashTable.New(
			X.KeyValue(java.util.ArrayList.class, 10043L),
			X.KeyValue(java.util.HashSet.class  ,    44L)
		);
		
		final PersistenceTypeRegistry typeRegistry = PersistenceTypeRegistry.New();
		Persistence.iterateJavaBasicTypes((type, systemTypeId) ->
		{
			typeRegistry.registerType(X.coalesce(customTypeIds.get(type), systemTypeId), type);
		});
		f.getConnectionFoundation().setTypeRegistry(typeRegistry);
	}

	public static void main(final String[] args)
	{
		if(STORAGE.root() == null)
		{
			Test.print("TEST: graph required." );
			STORAGE.setRoot(generateGraph());
			Test.print("STORAGE: storing ...");
			STORAGE.storeRoot();
			Test.print("STORAGE: storing completed.");
		}
		else
		{
			Test.print("TEST: graph loaded." );
			Test.print(STORAGE.root());
			Test.print("TEST: done." );
		}
		
		System.exit(0);
	}
	
	static ArrayList<String> generateGraph()
	{
		return OldCollections.ArrayList("A", "B", "C");
	}
		
}
