package one.microstream.storage.restservice;

import java.io.IOException;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;

public class TestDynamicHandlerCreation
{
	public static void main(final String[] args)
	{
		MyRoot myRoot = new MyRoot();
		
		final EmbeddedStorageManager storage = EmbeddedStorage.start(myRoot);
		final StorageRestService service = StorageRestServiceResolver.resolve(storage);
		service.start();
		
		if(myRoot.myInterface == null)
		{
			myRoot.myInterface = new MyImpl("Hello", "World");
			storage.storeRoot();
			
			System.out.println("Stored root with conent: " + myRoot.myInterface);
		}
		else
		{
			myRoot = (MyRoot) storage.root();
			System.out.println("loaded root from storage: " + myRoot.myInterface);
		}
		
		
		System.out.println("press any key to exit");
		try {
			System.in.read();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		
		service.stop();
		storage.shutdown();
		System.out.println("shutdown done");
	}
}
