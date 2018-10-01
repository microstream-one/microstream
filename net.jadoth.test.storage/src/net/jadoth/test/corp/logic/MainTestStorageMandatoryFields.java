package net.jadoth.test.corp.logic;

import net.jadoth.X;
import net.jadoth.collections.types.XList;
import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.storage.util.StoreEager;
import net.jadoth.test.corp.model.Address;
import net.jadoth.test.corp.model.Person;
import net.jadoth.time.XTime;


public class MainTestStorageMandatoryFields
{
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = X.executeOn(
		EmbeddedStorage.createFoundation(),
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
		if(STORAGE.root().get() == null)
		{
			// first execution enters here

			Test.print("TEST: model data required." );
			final XList<Person> persons = createTestData();
			STORAGE.root().set(persons);
			Test.print("STORAGE: storing #1 ...");
			STORAGE.store(STORAGE.root());
			Test.print("STORAGE: storing #2 ...");
			STORAGE.store(persons.at(0));
			Test.print("STORAGE: storing completed.");
		}
		else
		{
			// subsequent executions enter here

			Test.print("TEST: model data loaded." );
			Test.print(STORAGE.root().get());
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
