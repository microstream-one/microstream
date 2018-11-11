package net.jadoth.test.corp.logic;

import java.util.Arrays;

import net.jadoth.persistence.types.Persistence;
import net.jadoth.reference.Reference;
import net.jadoth.storage.types.EmbeddedStorage;


public class MainTestStorageTopLevelTypes
{
	public static void main(final String[] args)
	{
		final net.jadoth.reference.Reference<Object>               root         = Reference.New(null);

		final net.jadoth.persistence.types.PersistenceRootResolver rootResolver = Persistence.RootResolver(root);
		
		final net.jadoth.storage.types.EmbeddedStorageManager      storage      = EmbeddedStorage
			.Foundation()
			.setRootResolver(rootResolver)
			.start()
		;
		
		final net.jadoth.storage.types.StorageConnection           connection  = storage.createConnection();
		
		final net.jadoth.persistence.types.Storer                  storer      = connection.createStorer();
				
		final java.util.List<TestPerson> entityGraph = Arrays.asList(
			new TestPerson(),
			new TestPerson(),
			new TestPerson()
		);
		
		storer.store(entityGraph);
		storer.commit();
	}
	
}

class TestPerson
{
	java.lang.String                        firstName;
	java.lang.String                        lastName ;
	net.jadoth.swizzling.types.Lazy<String> hugeText ;
}



