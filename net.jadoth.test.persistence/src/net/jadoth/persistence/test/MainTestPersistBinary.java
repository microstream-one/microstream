package net.jadoth.persistence.test;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.binary.types.BinaryPersistenceFoundation;
import net.jadoth.persistence.types.PersistenceManager;

public class MainTestPersistBinary extends TestComponentProvider
{
	// actual persistence manager creation (automated and application-specific assembly)//
	static final BinaryPersistenceFoundation<?> foundation = BinaryPersistence.Foundation(dispatcher);
	static final PersistenceManager<Binary> persistenceManager =
		TEST
		.setFilenameData("BinaryTest.dat")
		.initialize(foundation)
		.createPersistenceManager()
	;


	// (15.05.2013 TM)NOTE:
	public static void main(final String[] args) throws Throwable
	{
//		System.out.println(DEBUG_BinaryPersistence.oidToString(oid));

//		final Object testObject = TestBinaryPersistenceTests.testObject();
//		TestBinaryPersistenceTests.testRegisterer(persistenceManager);
//		System.out.println(Native.isEqualObjectGraph(testObject, ((Object[])testObject).clone(), persistenceManager));

//		TestBinaryPersistenceTests.testReadStateDefs(factory);

//		factory.getTypeDefinitionsImporter().importTypeDefinitions();


//		TestBinaryPersistenceTests.debugPrintGraph(
//			TestBinaryPersistenceTests.testObject(),
//			persistenceManager,
//			foundation
//		);

		TestBinaryPersistenceTests.testPersist(persistenceManager);
//		TestBinaryPersistenceTests.testWriteStateDefs(factory);

//		TestBinaryPersistenceTests.testPersisterDirect(persistenceManager);

//		TestBinaryPersistenceTests.testFileLoading();

//		TestBinaryPersistenceTests.testReadStateDefs(factory);
//
//		TestBinaryPersistenceTests.testBuilding(persistenceManager);
//		TestBinaryPersistenceTests.testBuilding(persistenceManager);

		/* (03.07.2012 TM)TODO:
		 * Type Description loading test cases:
		 * - imported type mapping does not fit with local type mapping
		 * - type handler already exists, validates correct type description
		 * - type handler already exists, validates incorrect type description
		 * - type handler not yet exists, type description is correct for local class
		 * - type handler not yet exists, type description is incorrect for local class
		 */
	}

}
