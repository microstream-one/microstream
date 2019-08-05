package one.microstream.test.corp.main;

import java.util.Comparator;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStoreSyntheticClassesException
{
	public static void main(final String[] args)
	{
		final EmbeddedStorageManager storage = EmbeddedStorage.Foundation().start();
		
		if(storage.root() == null)
		{
			Test.print("Model data required.");
			storage.setRoot(
				new Comparator<String>() {
					@Override
					public int compare(final String o1, final String o2)
					{
						throw new one.microstream.meta.NotImplementedYetError(); // FIXME Comparator<String>#compare()
					}
				}
			);
			
			Test.print("Storing ...");
			storage.storeRoot();
			Test.print("Storing completed.");
		}
		else
		{
			// subsequent executions enter here (database reading)

			Test.print("Model data loaded.");
			Test.print("Root instance: " + storage.root());
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(storage, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}
		
		// no shutdown required, the storage concept is inherently crash-safe
		System.exit(0);
	}
		
}
