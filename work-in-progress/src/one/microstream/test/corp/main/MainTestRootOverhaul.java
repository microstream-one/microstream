package one.microstream.test.corp.main;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;
import one.microstream.time.XTime;


public class MainTestRootOverhaul
{
	public static void main(final String[] args)
	{
//		doCustomRoot();
		doDefaultRoot();
	}
	
	

	static final MyAppRoot ROOT = new MyAppRoot("Custom Root @" + XTime.now().toString());
	static void doCustomRoot()
	{
		final EmbeddedStorageManager storage = EmbeddedStorage.start(ROOT);
		doCommon(storage);
	}
	
	static void doDefaultRoot()
	{
		final EmbeddedStorageManager storage = EmbeddedStorage.start();
		if(storage.root() == null)
		{
			storage.setRoot(new MyAppRoot("Default Root @" + XTime.now().toString()));
			storage.storeRoot();
		}
		
		doCommon(storage);
	}
	
	static void doCommon(final EmbeddedStorageManager storage)
	{
		Test.printInitializationTime(storage);
		Test.printOperationModeTime(storage);
		Test.print("Model data loaded.");
		Test.print("Root instance: " + storage.root());
		
		Test.print("Exporting data ...");
		TestImportExport.testExport(storage, Test.provideTimestampedDirectory("testExport"));
		Test.print("Data export completed.");
		
		// no shutdown required, the storage concept is inherently crash-safe
		System.exit(0);
	}
	
	
	static class MyAppRoot
	{
		String rootName;

		MyAppRoot(final String rootName)
		{
			super();
			this.rootName = rootName;
		}
		
		@Override
		public final String toString()
		{
			return super.toString() + ": " + this.rootName;
		}
		
	}
	
}
