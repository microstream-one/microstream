package one.microstream.test.corp.logic;

import java.util.Arrays;

import one.microstream.persistence.types.Persistence;
import one.microstream.reference.Reference;
import one.microstream.storage.types.EmbeddedStorage;


public class MainTestStorageTopLevelTypes
{
	public static void main(final String[] args)
	{
		final one.microstream.reference.Reference<Object>               root         = Reference.New(null);

		final one.microstream.persistence.types.PersistenceRootResolver rootResolver = Persistence.RootResolver(root);
		
		final one.microstream.storage.types.EmbeddedStorageManager      storage      = EmbeddedStorage
			.Foundation()
			.setRootResolver(rootResolver)
			.start()
		;
		
		final one.microstream.storage.types.StorageConnection           connection  = storage.createConnection();
		
		final one.microstream.persistence.types.Storer                  storer      = connection.createStorer();
				
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
	one.microstream.persistence.lazy.Lazy<String> hugeText ;
}



