package one.microstream.storage.types;

import one.microstream.collections.EqHashTable;

public interface Databases
{
	public Database get(String identifier);
	
	public Database register(StorageManager storage);
	
	
	
	public static Databases get()
	{
		return Static.get();
	}

	public final class Static
	{
		private static final Databases SINGLETON = Databases.New();
		
		public static Databases get()
		{
			return SINGLETON;
		}
		
	}
	
	
	public static Databases New()
	{
		return new Databases.Default(EqHashTable.New());
	}
	
	public final class Default implements Databases
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final EqHashTable<String, Database> databases;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final EqHashTable<String, Database> databases)
		{
			super();
			this.databases = databases;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final synchronized Database get(final String identifier)
		{
			return this.databases.get(identifier);
		}

		@Override
		public final synchronized Database register(final StorageManager storage)
		{
			final String databaseIdentifier = storage.identifier();
			
			Database database = this.get(databaseIdentifier);
			if(database != null)
			{
				database.setStorage(storage);
			}
			else
			{
				database = Database.New(storage);
				this.databases.add(databaseIdentifier, database);
			}
			
			return database;
		}
		
	}
	
}
