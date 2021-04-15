package one.microstream.examples.storing;

import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

/**
 * Microstream data storing example  
 *
 */
public class Main 
{
    public static void main( String[] args )
    {
    	// Root instance
    	final MyRoot root = new MyRoot();

    	// Init storage manager
    	final EmbeddedStorageManager storageManager = EmbeddedStorage.start(root);
    
    	//store the root object
    	storageManager.storeRoot();
    	
    	//add a new data object to the list in root 
    	MyData dataItem  = new MyData("Alice");
    	root.myObjects.add(dataItem);
    	
    	//store the modified list
    	storageManager.store(root.myObjects);
    	
    	//modify a value type member and store it
    	dataItem .setIntValue(100);
    	storageManager.store(dataItem);
    	
    	//modify a string object and store it
    	dataItem .setName("Bob");
    	storageManager.store(dataItem);
    	    	
    	
    	//shutdown 
    	storageManager.shutdown();    	
    }
}
