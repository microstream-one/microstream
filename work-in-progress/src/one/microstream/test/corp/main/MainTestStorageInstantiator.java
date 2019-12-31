package one.microstream.test.corp.main;

import one.microstream.collections.types.XGettingTable;
import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.hashing.HashStatistics;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceInstantiator;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;
import one.microstream.test.corp.model.Person;


public class MainTestStorageInstantiator
{
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.Foundation()
		.onConnectionFoundation(e ->
			e
			.setInstantiator(MainTestStorageInstantiator::instantiate)
			.registerCustomInstantiator(Person.class, MainTestStorageInstantiator::instantiatePerson)
		)
		.start()
	;
	
	public static <T> T instantiate(final Class<T> type, final Binary data)
		throws InstantiationRuntimeException
	{
		System.out.print("  Creating a blank " + type.getSimpleName() + " instance of ... ");
		final T instance = PersistenceInstantiator.instantiateBlank(type);
		System.out.println("created @" + Integer.toHexString(System.identityHashCode(instance)));
		
		return instance;
	}
	
	public static Person instantiatePerson(final Binary data)
		throws InstantiationRuntimeException
	{
		System.out.print("* Creating a constructed " + Person.class.getSimpleName() + " instance ... ");
		final Person instance = new Person();
		System.out.println("created @" + Integer.toHexString(System.identityHashCode(instance)));
		
		return instance;
	}

	public static void main(final String[] args)
	{
		// either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.defaultRoot().get() == null)
		{
			
			// first execution enters here

			Test.print("TEST: model data required." );
			STORAGE.defaultRoot().set(Test.generateModelData(100));
			Test.print("STORAGE: storing ...");
			STORAGE.storeDefaultRoot();
//			STORAGE.issueFullFileCheck();
			Test.print("STORAGE: storing completed.");
//			printObjectRegistryStatistics();
		}
		else
		{
			// subsequent executions enter here

			Test.print("TEST: model data loaded." );
			Test.print(STORAGE.defaultRoot().get());
			Test.print("TEST: exporting data ..." );
//			printObjectRegistryStatistics();
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testCorpExport"));
			Test.print("TEST: data export completed.");
		}
		
		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
	
	static void printObjectRegistryStatistics()
	{
		final XGettingTable<String, ? extends HashStatistics> stats =
			STORAGE.persistenceManager().objectRegistry().createHashStatistics()
		;
		stats.iterate(e ->
		{
			System.out.println(e.key());
			System.out.println(e.value());
		});
	}
	
}
