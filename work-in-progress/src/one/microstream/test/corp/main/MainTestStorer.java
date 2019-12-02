package one.microstream.test.corp.main;

import one.microstream.io.XIO;
import one.microstream.meta.XDebug;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageFileProvider;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStorer
{
	static
	{
		XDebug.deleteAllFiles(XIO.Path(StorageFileProvider.Defaults.defaultStorageDirectory()), true);
	}
	
	// creates and starts an embedded storage manager with all-default-settings.

	public static void main(final String[] args)
	{
		final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();
		
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root() == null)
		{
			// first execution enters here (database creation)

			Test.print("Model data required.");
			STORAGE.setRoot(generateModelData());
			
			Test.print("Storing ...");
			STORAGE.storeRoot();
			Test.print("Storing completed.");
		}
		else
		{
			// subsequent executions enter here (database reading)

			Test.printInitializationTime(STORAGE);
			Test.printOperationModeTime(STORAGE);
			Test.print("Model data loaded.");
			Test.print("Root instance: " + STORAGE.root());
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}
		
		STORAGE.shutdown();
		
//		while(STORAGE.isActive())
//		{
//			XThreads.sleep(10);
//		}
//		System.err.println(STORAGE.isActive());
//
//		STORAGE = null;
//		System.gc();
		
//		XThreads.sleep(1000);
		
		XDebug.deleteAllFiles(XIO.Path(StorageFileProvider.Defaults.defaultStorageDirectory()), true);
		
		// no shutdown required, the storage concept is inherently crash-safe
		System.exit(0);
	}
	
	static Entity generateModelData()
	{
		final Entity root = new Entity().setName("root");
		final Entity e1 = new Entity().setName("e1");
		root.setOther(e1);
		final Entity e2 = new Entity().setName("e2");
		e1.setOther(e2);
		e2.setOther(root);
		
		return root;
	}
	
	
	static class Entity
	{
		String name;
		Entity other;
		
		public Entity setName(final String name)
		{
			this.name = name;
			return this;
		}
		
		public Entity setOther(final Entity other)
		{
			this.other = other;
			return this;
		}
		
	}
	
}
