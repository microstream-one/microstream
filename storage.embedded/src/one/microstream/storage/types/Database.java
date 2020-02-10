package one.microstream.storage.types;

import java.lang.ref.WeakReference;

import one.microstream.storage.exceptions.StorageException;

public interface Database
{
	public String identifier();
	
	public StorageManager storage();
	
	public StorageManager setStorage(StorageManager storage);
	
	
	
	public static Database New(final StorageManager storage)
	{
		return new Database.Default(
			storage.identifier(),
			new WeakReference<>(storage)
		);
	}
	
	public final class Default implements Database
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String identifier;
		
		private WeakReference<StorageManager> storageReference;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final String identifier, final WeakReference<StorageManager> storageReference)
		{
			super();
			this.identifier = identifier;
			this.storageReference = storageReference;
		}
		
		

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String identifier()
		{
			return this.identifier;
		}

		@Override
		public final synchronized StorageManager storage()
		{
			return this.storageReference.get();
		}
		
		@Override
		public final synchronized StorageManager setStorage(final StorageManager storage)
		{
			final StorageManager existingStorage = this.storage();
			if(existingStorage != null)
			{
				// (10.02.2020 TM)EXCP: proper exception
				throw new StorageException(
					"Storage for identifier \"" + this.identifier + "\" already exists."
				);
			}
			
			final String otherIdentifier = storage.identifier();
			if(!this.identifier.equals(otherIdentifier))
			{
				// (10.02.2020 TM)EXCP: proper exception
				throw new StorageException(
					"Storage identifier mismatch: Cannot assign "
					+ StorageManager.class.getSimpleName() + " named \"" + otherIdentifier
					+ "\" to a database named " + "\"" + this.identifier + "\"."
				);
			}
			
			// other storage instance can be set validly/consistently.
			this.storageReference = new WeakReference<>(storage);
			
			return storage;
		}
		
	}
	
}
