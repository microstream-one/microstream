package one.microstream.examples.deleting;

import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

/**
 * 
 * Microstream deleting data example
 * 
 * Deleting an (data)object means to remove all references to the object:
 * In this example all data objects are referenced from the myObjects list of the
 * MyRoot class. Thus deleting an object is done by removing it's reference from the
 * myObjects list and storing the myObject list again.
 * 
 * Objects that are no more reachable in the stored object graph 
 * will be removed from the storage files a a later time.
 * 
 */
public class Main 
{
	public static void main(String[] args) 
	{
    	//Init storage manager
    	final EmbeddedStorageManager storage = EmbeddedStorage.start();
    	    	
    	//if storage.root() returns null no data has been loaded
    	//since there is no existing database, let's create a new one.
    	if(storage.root() == null)
    	{
    		System.out.println("No existing Database found, creating a new one:");
    		
    		MyRoot root = new MyRoot();
    		storage.setRoot(root);
    		root.myObjects.add(new MyData("Alice", 20));
    		root.myObjects.add(new MyData("Bob"  , 25));
    		root.myObjects.add(new MyData("Claire", 18));
    		storage.storeRoot();    		
    		
    		root.myObjects.forEach(System.out::println);
    		System.out.println("\n");
    	}
    	//storage.root() is not null so we have loaded data
    	else
    	{
    		System.out.println("Existing Database found:");
    		
    		MyRoot root = (MyRoot) storage.root();
    		root.myObjects.forEach(System.out::println);    		    		    		   
    		
    		if(!root.myObjects.isEmpty())
    		{
    			System.out.println("\nDeleting first object:");
    			
    			//delete the first object from our object graph
	    		root.myObjects.remove(0);	    			    			    		
	    		
	    		//store the changed list to apply the deletion of it's second element
	    		storage.store(root.myObjects);
	    		
	    		if(!root.myObjects.isEmpty())
	    		{
	    			root.myObjects.forEach(System.out::println);
	    		}
	    		else 
	    		{
	    			System.out.println("myObjects is empty!");
	    		}
    		}
    		else
    		{
    			System.out.println("myObjects is empty!");
    		}
    	}
    	
    	storage.shutdown();
   	}

}
