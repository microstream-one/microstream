package one.microstream.test.corp.main;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStorageStringStuff
{
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();

	public static void main(final String[] args)
	{
		if(STORAGE.defaultRoot().get() == null)
		{
			STORAGE.defaultRoot().set(createObjectGraph());
			Test.print("STORAGE: storing ...");
			STORAGE.storeDefaultRoot();
			Test.print("STORAGE: storing completed.");
		}
		else
		{
			Test.print("TEST: model data loaded." );
			printObjectGraph((Object[])STORAGE.defaultRoot().get());
			Test.print("TEST: exporting data ..." );
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testCorpExport"));
			Test.print("TEST: data export completed.");
		}
		System.exit(0);
	}
	
	static void printObjectGraph(final Object[] objectGraph)
	{
		for(final Object o : objectGraph)
		{
			System.out.println(o);
		}
	}
	
	static Object[] createObjectGraph()
	{
		return new Object[]{
			"A String",
			"Another String",
			new StringBuilder("StringBuilder string"),
			new StringBuffer("StringBuffer string")
		};
	}
	
}
