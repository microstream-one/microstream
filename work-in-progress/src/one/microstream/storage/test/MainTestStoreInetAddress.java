package one.microstream.storage.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStoreInetAddress
{
	static
	{
//		Test.clearDefaultStorageDirectory();
	}
	
	// creates and starts an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();

	public static void main(final String[] args) throws UnknownHostException
	{
		final InetAddress iaByAddress1 = InetAddress.getByAddress(X.toBytes(0xFF_80_80_01));
		final InetAddress iaByAddress2 = InetAddress.getByAddress("localhost", X.toBytes(0xFF_80_80_02));
		final InetAddress iaByName1    = InetAddress.getByName("localhost");
		final InetAddress iaByName2    = InetAddress.getByName("127.0.0.1");
	
		// (02.03.2020 TM)FIXME: priv#117: test InetAddres v4 and v6.
		
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root() == null)
		{
			// first execution enters here (database creation)

			Test.print("Model data required.");
			STORAGE.setRoot(iaByAddress1);
			
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
			final Object loadedRoot = STORAGE.root();
			Test.print("Root instance: " + XChars.systemString(loadedRoot)+ " = " + loadedRoot);
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}
		
		STORAGE.shutdown();
		System.exit(0);
	}
		
}
