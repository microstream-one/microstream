package one.microstream.test.binary_fields;

import java.util.Date;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;
import one.microstream.time.XTime;


public class MainTestStorageBinaryFields
{
	static
	{
//		Test.clearDefaultStorageDirectory();
	}
	
	static final BFTestLeaf APP_ROOT = new BFTestLeaf(Byte.MAX_VALUE, Short.MAX_VALUE, Integer.MAX_VALUE);
	
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start(APP_ROOT);
		
		 // explicit registration no longer needed with PersistenceTypeHandlerProviding
//		.Foundation()
//		.registerTypeHandler(new BinaryHandlerBFTestLeaf())
//		.start(APP_ROOT)
//	;

	public static void main(final String[] args)
	{
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(APP_ROOT.arString1 == null)
		{
			// first execution enters here (database creation)
			Test.print("Model data required.");
			initializeData(APP_ROOT);
			
			Test.print("Storing ...");
			STORAGE.storeRoot();
			Test.print("Storing completed.");
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}
		else
		{
			// subsequent executions enter here (database reading)
			Test.printInitializationTime(STORAGE);
			Test.printOperationModeTime(STORAGE);
			Test.print("Model data loaded.");
			Test.print("Root instance: " + APP_ROOT);
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}

		System.exit(0);
	}
	
	static void initializeData(final BFTestLeaf root)
	{
		final Date now = XTime.now();
		final long id = now.getTime() % 10000L;
		
		root.aDerivedDate = now;
		root.arString1 = "Some String @ " + now;
		root.aDerivedString = String.valueOf(id);
		root.ip_char = root.arString1.charAt(root.arString1.length() - 1);
		root.lp_float = 0.1f * id;
		root.lp_double = 0.01d * id;
		root.lp_long = id * 2;
	}
	
}
