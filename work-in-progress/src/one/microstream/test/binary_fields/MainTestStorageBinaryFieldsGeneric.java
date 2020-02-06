package one.microstream.test.binary_fields;

import java.util.ArrayList;
import java.util.List;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;
import one.microstream.time.XTime;


public class MainTestStorageBinaryFieldsGeneric
{
	static final List<Employee> APP_ROOT = new ArrayList<>();
	
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start(APP_ROOT);

	public static void main(final String[] args)
	{
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(APP_ROOT.isEmpty())
		{
			// first execution enters here (database creation)
			Test.print("Model data required.");
			initializeData(APP_ROOT, 10);
			
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
	
	static void initializeData(final List<Employee> root, final int count)
	{
		for(int i = 1; i <= count; i++)
		{
			root.add(new Employee(
				String.valueOf(i),
				30_000.00 + i * 1_000.00,
				XTime.date(1980, 1 + i / 28, 1 + i % 28))
			);
		}
	}
	
}
