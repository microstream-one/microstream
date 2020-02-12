package one.microstream.test.basic;

import java.util.Date;

import one.microstream.chars.XChars;
import one.microstream.reference.Lazy;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;
import one.microstream.time.XTime;


public class MainTestStorageDatabase
{
	static
	{
		Test.clearDefaultStorageDirectory();
	}
	
	// Option 1: Explicit application root provided at startup (specific typing)
	static final AppRoot<Lazy<Date>> APP_ROOT = new AppRoot<>();
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start(APP_ROOT);

	public static void main(final String[] args)
	{
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(APP_ROOT.value == null)
		{
			// first execution enters here (database creation)
			Test.print("Model data required.");
			final Lazy<Date> ref = Lazy.Reference(XTime.now());
			APP_ROOT.set(ref);
			
			Test.print("Storing ...");
			STORAGE.storeRoot();
			Test.print("Storing completed.");
			
			// just for fun
//			final Databases dbs = Databases.get();
//			System.out.println(dbs);
						
			ref.clear();
			
			STORAGE.shutdown();
			
			// must throw an exception since storage manager is not running. (comment out to test working case)
//			System.out.println(ref.get());
			
			final EmbeddedStorageManager replacementStorage = EmbeddedStorage.start(APP_ROOT);
			System.out.println("Replacement: " + XChars.systemString(replacementStorage));
			
			// now it must work again since the new storage manager has replaced the old one in the database instance.
			System.out.println(ref.get());
			
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
			Test.print("Root instance: " + Lazy.get(APP_ROOT.value));
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}
		
		STORAGE.shutdown();
		
		// no shutdown required, the storage concept is inherently crash-safe
		System.exit(0);
	}
	
}
