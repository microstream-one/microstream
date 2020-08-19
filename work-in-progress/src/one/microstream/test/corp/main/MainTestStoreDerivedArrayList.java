package one.microstream.test.corp.main;

import java.util.ArrayList;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStoreDerivedArrayList
{
	static
	{
//		Test.clearDefaultStorageDirectory();
	}
	
	// creates and starts an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();

	public static void main(final String[] args)
	{
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root() == null)
		{
			// first execution enters here (database creation)

			Test.print("Model data required.");
			STORAGE.setRoot(new MyArrayList<>("1", "2", "3"));
			
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
			Test.print("Root instance: " + STORAGE.root());
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}

		// no shutdown required, the storage concept is inherently crash-safe
//		STORAGE.shutdown();
		
		System.exit(0);
	}
		
}


class MyArrayList<E> extends ArrayList<E>
{
	@SafeVarargs
	MyArrayList(final E... elements)
	{
		super(elements == null ? 0 : elements.length);
		if(elements != null)
		{
			for(final E e : elements)
			{
				this.add(e);
			}
		}
	}
}
