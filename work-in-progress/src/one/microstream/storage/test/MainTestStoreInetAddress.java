package one.microstream.storage.test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.java.net.AbstractBinaryHandlerInetAddress;
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
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root() == null)
		{
			// first execution enters here (database creation)

			Test.print("Model data required.");
			STORAGE.setRoot(createTestRoot());
			
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
			final Object[] loadedRoot = (Object[])STORAGE.root();
			Test.print("Root instance: " + XChars.systemString(loadedRoot)+ " = " + Arrays.toString(loadedRoot));
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}
		
		STORAGE.shutdown();
		System.exit(0);
	}
	
	public static InetAddress[] createTestRoot() throws UnknownHostException
	{
		final byte[] address1 = AbstractBinaryHandlerInetAddress.parseIpV6Address("2001:0db8:0000:0000:0000:8a2e:0370:7");
		final byte[] address2 = AbstractBinaryHandlerInetAddress.parseIpV6Address("2001:db8::8a2e:370:7");
		
		return new InetAddress[]
		{
			InetAddress.getByAddress(X.toBytes(0xFF_80_80_01)),
			InetAddress.getByAddress("localhost", X.toBytes(0xFF_80_80_02)),
			InetAddress.getByName("localhost"),
			InetAddress.getByName("127.0.0.1"),
			InetAddress.getByAddress(address1),
			InetAddress.getByAddress("localhost", address2),
			InetAddress.getByName("localhost"),
			InetAddress.getByName("2001:db8::8a2e:370:7")
		};
	}
		
}
