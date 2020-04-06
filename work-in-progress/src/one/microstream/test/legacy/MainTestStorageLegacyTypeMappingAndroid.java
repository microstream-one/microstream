package one.microstream.test.legacy;

import one.microstream.memory.android.MicroStreamAndroidAdapter;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStorageLegacyTypeMappingAndroid
{
	static
	{
		MicroStreamAndroidAdapter.setupFull();
		
		// (25.03.2020 TM)NOTE: disable for second execution
		Test.clearDefaultStorageDirectory();
	}

	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.Foundation().start();

	public static void main(final String[] args)
	{
		// either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root() == null)
		{
			// first execution enters here

			Test.print("TEST: graph required." );
			STORAGE.setRoot(generateGraph());
			Test.print("STORAGE: storing ...");
			STORAGE.storeRoot();
			Test.print("STORAGE: storing completed.");
		}
		else
		{
			// subsequent executions enter here

			Test.print("TEST: graph loaded." );
			final Object root = STORAGE.root();
			Test.print(root);
			Test.print("TEST: exporting data ..." );
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory(MainTestStorageLegacyTypeMappingAndroid.class.getName()));
			Test.print("TEST: data export completed.");
		}

		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}

	static Object generateGraph()
	{
		return ByteLegacy.createSample();
	}


	static class ByteLegacy
	{
		byte byteTo_boolean;
		byte byteTo_byte;
		byte byteTo_char;
		byte byteTo_double;
		byte byteTo_float;
		byte byteTo_int;
		byte byteTo_long;
		byte byteTo_short;
		byte byteToByte;
		Byte copyByteTo_byte;

		public static Object createSample()
		{
			final ByteLegacy legacy = new ByteLegacy();
			legacy.byteTo_boolean  = '0';
			legacy.byteTo_byte     = 'a';
			legacy.byteTo_char     = 'c';
			legacy.byteTo_double   = '2';
			legacy.byteTo_float    = '1';
			legacy.byteTo_int      = '6';
			legacy.byteTo_long     = '4';
			legacy.byteTo_short    = '3';
			legacy.byteToByte      = 'x';
			legacy.copyByteTo_byte = 'i';

			return legacy;
		}
	}

	static class ByteLegacy2
	{
		boolean byteTo_boolean;
		byte    byteTo_byte;
		char    byteTo_char;
		double  byteTo_double;
		float   byteTo_float;
		int     byteTo_int;
		long    byteTo_long;
		short   byteTo_short;
		Byte    byteToByte;
		byte    copyByteTo_byte;


		public static Object createSample()
		{
			throw new one.microstream.meta.NotImplementedYetError();
		}
	}

}
