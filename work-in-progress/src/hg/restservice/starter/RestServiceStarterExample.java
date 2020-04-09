package hg.restservice.starter;

import one.microstream.storage.restservice.StorageRestService;
import one.microstream.storage.restservice.StorageRestServiceResolver;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;

public class RestServiceStarterExample
{
	 public static void main(final String[] args)
     {
         final EmbeddedStorageManager storage = EmbeddedStorage.start();
         if(storage.root() == null)
         {
              storage.setRoot(new Object[] {"A", "B", "C"});
              storage.storeRoot();
         }

         final StorageRestService service = StorageRestServiceResolver.resolve(storage);
         service.start();
     }
}