package one.microstream.test.corp.logic;

import one.microstream.X;
import one.microstream.collections.types.XList;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.util.StoreEager;
import one.microstream.test.corp.model.Address;
import one.microstream.test.corp.model.Person;
import one.microstream.time.XTime;


public class MainTestStorageMandatoryFields
{
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = X.on(
		EmbeddedStorage.Foundation(),
		esf ->
			esf.getConnectionFoundation().setReferenceFieldMandatoryEvaluator((clazz, field) ->
//				clazz == Person.class && field.getType() == Address.class
				field.getAnnotation(StoreEager.class) != null
			)
		)
		.start()
	;

	public static void main(final String[] args)
	{
		// either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.defaultRoot().get() == null)
		{
			// first execution enters here

			Test.print("TEST: model data required." );
			final XList<Person> persons = createTestData();
			STORAGE.defaultRoot().set(persons);
			Test.print("STORAGE: storing #1 ...");
			STORAGE.storeDefaultRoot();
			Test.print("STORAGE: storing #2 ...");
			STORAGE.store(persons.at(0));
			Test.print("STORAGE: storing completed.");
		}
		else
		{
			// subsequent executions enter here

			Test.print("TEST: model data loaded." );
			Test.print(STORAGE.defaultRoot().get());
			Test.print("TEST: exporting data ..." );
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testCorpExport"));
			Test.print("TEST: data export completed.");
		}
		

//		STORAGE.shutdown();
		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
	
	
	static XList<Person> createTestData()
	{
		final Address a = new Address(null, null, null);
		final Person p1 = new Person(XTime.now().toString(), "p1", "GenericGuy1", a);
		final Person p2 = new Person(XTime.now().toString(), "p2", "GenericGuy2", a);
		
		return X.List(p1, p2);
	}
}
