package one.microstream.test.legacy;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import one.microstream.collections.old.OldCollections;
import one.microstream.persistence.internal.InquiringLegacyTypeMappingResultor;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResultor;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;
import one.microstream.time.XTime;


public class MainTestStorageLegacyTypeMapping
{
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.Foundation()
		.onConnectionFoundation(f ->
			f.setLegacyTypeMappingResultor(
				InquiringLegacyTypeMappingResultor.New(
					PersistenceLegacyTypeMappingResultor.New(), 0.8
				)
			)
		)
		.setRefactoringMappingProvider(
			Persistence.RefactoringMapping(Paths.get("Refactorings.csv"))
		)
		.start()
	;

	public static void main(final String[] args)
	{
		// either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root() == null)
		{
			// first execution enters here

			Test.print("TEST: graph required." );
			STORAGE.setRoot(generateGraph());
			Test.print("STORAGE: storing ...");
			STORAGE.storeRoot();
			Test.print("STORAGE: storing completed.");
		}
		else
		{
			// subsequent executions enter here

			Test.print("TEST: graph loaded." );
			Test.print(STORAGE.root());
			Test.print("TEST: exporting data ..." );
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory(MainTestStorageLegacyTypeMapping.class.getName()));
			Test.print("TEST: data export completed.");
		}

		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}

	static Object generateGraph()
	{
//		return new User137();
//		return new Person();
		return new TestEntity(47, "testi", "A", "B", "C");
	}

	static class TestEntity
	{
		Integer            id       ;
//		String             name     ;
		Date               stuff    ;
		ArrayList<String>  moreStuff;
		ArrayList<Integer> newStuff ;
		String             name1     ;
		String             name2     ;
		String             name3     ;
//		String             lastName     ;


		public TestEntity(final Integer id, final String name, final String... moreStuff)
		{
			super();
			this.id = id;
//			this.name = name;
			this.stuff = XTime.now();
			this.moreStuff = OldCollections.ArrayList(moreStuff);
//			this.lastName = name;
		}


		@Override
		public String toString()
		{
			return "TestEntity [id=" + this.id
//				+ ", name=" + this.name
//				+ ", name=" + this.lastName
				+ ", name1=" + this.name1
				+ ", name2=" + this.name2
				+ ", stuff=" + this.stuff
				+ ", moreStuff=" + this.moreStuff
				+ "]"
			;
		}

	}

	// "137": to test legacy type mapping with inheritance. There was a (now fixed) bug in the TypeId registration order.
	static class Person137
	{
		String fieldp1;
		String fieldp2;
	}

	static class User137 extends Person137
	{
//		String fieldu3;
		String fieldu4;
	}

}
