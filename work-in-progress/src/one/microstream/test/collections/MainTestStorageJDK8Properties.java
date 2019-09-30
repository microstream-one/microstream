package one.microstream.test.collections;

import java.util.Properties;
import java.util.UUID;

import one.microstream.reference.Reference;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStorageJDK8Properties
{
	static final Reference<Object[]>    ROOT    = Reference.New(null)        ;
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.Foundation()
		// (29.09.2019 TM)FIXME: cannot bring maven to replace the old dependencies with current version.
//		.onConnectionFoundation(f ->
//		{
//			f.getCustomTypeHandlerRegistry().registerTypeHandler(BinaryHandlerProperties.New());
//		})
		.start(ROOT);

	public static void main(final String[] args)
	{
		if(ROOT.get() == null)
		{
			System.out.println("Storing collections ...");
			ROOT.set(createRoot());
			STORAGE.store(ROOT);
			System.out.println("Stored collections.");
		}
		else
		{
			System.out.println("Loaded collections.");
			Test.print("Exporting collection data ..." );
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testCollections"));
		}
		
		for(final Object e : ROOT.get())
		{
			System.out.println(e.getClass().getSimpleName() + ":");
			System.out.println(e);
			System.out.println("\n");
		}
		
		System.exit(0);
	}
	
	
	private static Object[] createRoot()
	{
		final Properties defaults = populate(new Properties());
		
		return new Object[]{
			populate(new Properties(defaults))
		};
	}
		
	private static Properties populate(final Properties properties)
	{
		for(int i = 0; i < 10; i++)
		{
			properties.put("" + i, UUID.randomUUID().toString());
		}
		return properties;
	}
		
}

