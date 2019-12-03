package one.microstream.test.corp.main;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;


public class MainTestStorerSkipping
{
	static
	{
//		XDebug.deleteAllFiles(XIO.Path(StorageFileProvider.Defaults.defaultStorageDirectory()), true);
	}
	
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();
	
	// creates and starts an embedded storage manager with all-default-settings.

	public static void main(final String[] args)
	{
		// object graph with root either loaded on startup from an existing DB or required to be generated.
		if(STORAGE.root() == null)
		{
			// first execution enters here (database creation)

			Test.print("Model data required.");
			final Entity rootEntity = generateModelDataRoot();
//			fillRootEntity(rootEntity);
			STORAGE.setRoot(rootEntity);
			
			Test.print("Storing ...");
			STORAGE.storeRoot();
			Test.print("Storing completed.");

//			Test.print("Storing with skip ...");
//			fillRootEntity(rootEntity);
//			final Storer storer = STORAGE.createStorer();
//			storer.skipNulled(rootEntity.other.other); // root other is e2
//			storer.store(rootEntity);
//			storer.commit();
//			Test.print("Storing with skip completed.");
		}
		else
		{
			// subsequent executions enter here (database reading)

			Test.printInitializationTime(STORAGE);
			Test.printOperationModeTime(STORAGE);
			Test.print("Model data loaded.");
			Test.print("Root instance: " + STORAGE.root());
			
			Test.print("Exporting data ...");
			TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory("testExport"));
			Test.print("Data export completed.");
		}
		
		STORAGE.shutdown();
		
		// no shutdown required, the storage concept is inherently crash-safe
		System.exit(0);
	}
	
	static Entity generateModelDataRoot()
	{
		final Entity root = new Entity().setName("root");
		
		return root;
	}
	
	static Entity generateModelData()
	{
		return fillRootEntity(generateModelDataRoot());
	}
	
	static Entity fillRootEntity(final Entity root)
	{
		final Entity e1 = new Entity().setName("e1");
		root.setOther(e1);
		final Entity e2 = new Entity().setName("e2");
		e1.setOther(e2);
		e2.setOther(root);
		
		return root;
	}
	
	
	static class Entity
	{
		String name;
		Entity other;
		
		public Entity setName(final String name)
		{
			this.name = name;
			return this;
		}
		
		public Entity setOther(final Entity other)
		{
			this.other = other;
			return this;
		}
		
		@Override
		public String toString()
		{
			return Entity.class.getSimpleName() + " " + this.name;
		}
		
	}
	
}
