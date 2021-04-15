package one.microstream.examples.loading;

import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;


/**
 * 
 * A very simple example how to load an existing database
 * Run twice, the first run creates a database, all further runs
 * load it.
 *
 */
public class Main 
{
	public static void main(String[] args) 
	{  	    	 
    	// Init storage manager
    	final EmbeddedStorageManager storage = EmbeddedStorage.start();
    	    	
    	//if storage.root() returns null no data have been loaded
    	//since there is no existing database, let's create a new one.
    	if(storage.root() == null)
    	{
    		System.out.println("No existing Database found, creating a new one:");
    		
    		MyRoot root = new MyRoot();
    		storage.setRoot(root);
    		root.myObjects.add(new MyData("Alice", 20));
    		root.myObjects.add(new MyData("Bob"  , 25));
    		storage.storeRoot();    		
    	}
    	//storage.root() is not null so we have loaded data
    	else
    	{
    		System.out.println("Existing Database found:");
    		
    		MyRoot root = (MyRoot) storage.root();
    		root.myObjects.forEach(System.out::println);    		
    	}
    	
    	storage.shutdown();
	}
}
