package one.microstream.storage.test;

import one.microstream.chars.XChars;
import one.microstream.persistence.types.Storer;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;

public class MainTestStoringRobustness
{
	static
	{
		Test.clearDefaultStorageDirectory();
	}
	
	// creates and starts an embedded storage manager with all-default-settings.
	static final AppRoot                ROOT    = new AppRoot(null);
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start(ROOT);

	public static void main(final String[] args)
	{
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(ROOT.referent == null)
		{
			// first execution enters here (database creation)
			System.gc(); // to clean up the initialization storer

			Test.print("Model data required.");
			Storer storer1 = STORAGE.createStorer();
			Storer storer2 = STORAGE.createStorer();
			
			final String s = "Hello World";
			ROOT.referent = s;
			storer1.store(ROOT);
			
			// TEST: must internally lookup s' objectId in storer1 instead of assigning another one!
			storer2.store(s);
			
			storer1.commit();
			storer2.commit();
			
			// otherwise, storer1 won't get cleared by the GC below and it's not understandable why as scope runs out.
			storer1 = null;
			storer2 = null;
			
			Test.print("Merged ObjectId of 's' is: " + STORAGE.persistenceManager().lookupObjectId(s));
			
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
			final Object loadedRoot = STORAGE.root();
			Test.print("Root instance: " + XChars.systemString(loadedRoot)+ " = " + loadedRoot);
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}

		System.gc(); // to clean up the two storers
		
		STORAGE.shutdown();
		System.exit(0);
	}
	
}

class AppRoot
{
	Object referent;

	AppRoot(final Object referent)
	{
		super();
		this.referent = referent;
	}
	
}