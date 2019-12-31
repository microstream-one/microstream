package one.microstream.test.corp.main;

import java.math.BigDecimal;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStorageValueTypeRoot
{
	static final BigDecimal APP_ROOT = BigDecimal.valueOf(456); // change value to test value type validation
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start(APP_ROOT);

	public static void main(final String[] args)
	{
		Test.print("Storing ...");
		STORAGE.storeRoot();
		Test.print("Storing completed.");
		
		Test.print("Exporting data ...");
		TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
		Test.print("Data export completed.");

		System.exit(0);
	}
	
}
