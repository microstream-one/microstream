package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.lang.ref.WeakReference;

import one.microstream.chars.XChars;
import one.microstream.persistence.types.Persister;
import one.microstream.persistence.types.Storer;
import one.microstream.storage.exceptions.StorageExceptionConsistency;
import one.microstream.storage.exceptions.StorageExceptionInitialization;
import one.microstream.storage.exceptions.StorageExceptionNotActive;

public interface Database extends DatabasePart, Persister
{
	public default String toIdentifyingString()
	{
		return XChars.systemString(this) + " \"" + this.databaseName() + "\"";
	}
	
	public StorageManager storage();
	
	public StorageManager setStorage(StorageManager storage);
	
	public default boolean hasStorage()
	{
		return this.storage() != null;
	}
	
	public Database guaranteeNoActiveStorage();

	public StorageManager guaranteeActiveStorage();
	
	
	
	
	public static Database New(final String databaseName)
	{
		return new Database.Default(
			databaseName,
			new WeakReference<>(null)
		);
	}
	
	public final class Default implements Database
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String name;
		
		private WeakReference<StorageManager> storageReference;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final String identifier, final WeakReference<StorageManager> storageReference)
		{
			super();
			this.name = identifier;
			this.storageReference = storageReference;
		}
		
		

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String databaseName()
		{
			return this.name;
		}

		@Override
		public final synchronized StorageManager storage()
		{
			return this.storageReference.get();
		}
		
		@Override
		public final synchronized Database guaranteeNoActiveStorage()
		{
			final StorageManager existingStorage = this.storage();
			if(existingStorage != null && existingStorage.isRunning())
			{
				throw new StorageExceptionInitialization(
					"Active storage for " + this.toIdentifyingString() + " already exists."
				);
			}
			
			return this;
		}
		
		@Override
		public final synchronized StorageManager guaranteeActiveStorage()
		{
			final StorageManager existingStorage = this.storage();
			if(existingStorage == null)
			{
				throw new StorageExceptionInitialization(
					"No storage for " + this.toIdentifyingString() + " exists."
				);
			}
			
			if(!existingStorage.isRunning())
			{
				throw new StorageExceptionNotActive(
					"Storage for " + this.toIdentifyingString() + " is not active."
				);
			}
			
			return existingStorage;
		}
		
		@Override
		public final synchronized StorageManager setStorage(final StorageManager storage)
		{
			this.guaranteeNoActiveStorage();
			
			final Database associatedDatabase = storage.database();
			if(associatedDatabase != this)
			{
				throw new StorageExceptionConsistency(
					"Inconsistent database association: the passed " + StorageManager.class.getSimpleName()
					+ " belongs to " + associatedDatabase.toIdentifyingString()
					+ ", which is incompatible to this: " + this.toIdentifyingString() + "."
				);
			}
			
			// other storage instance can be set validly/consistently.
			this.storageReference = new WeakReference<>(storage);
			
			return storage;
		}
		
		@Override
		public final Object getObject(final long objectId)
		{
			final StorageManager storage = this.guaranteeActiveStorage();

			return storage.getObject(objectId);
		}
		
		@Override
		public final long store(final Object instance)
		{
			final StorageManager storage = this.guaranteeActiveStorage();

			return storage.store(instance);
		}
		
		@Override
		public final long[] storeAll(final Object... instances)
		{
			final StorageManager storage = this.guaranteeActiveStorage();

			return storage.storeAll(instances);
		}
		
		@Override
		public final void storeAll(final Iterable<?> instances)
		{
			final StorageManager storage = this.guaranteeActiveStorage();

			storage.storeAll(instances);
		}
		
		@Override
		public final Storer createLazyStorer()
		{
			final StorageManager storage = this.guaranteeActiveStorage();

			return storage.createLazyStorer();
		}
		
		@Override
		public final Storer createStorer()
		{
			final StorageManager storage = this.guaranteeActiveStorage();

			return storage.createStorer();
		}

		@Override
		public final Storer createEagerStorer()
		{
			final StorageManager storage = this.guaranteeActiveStorage();

			return storage.createEagerStorer();
		}
		
	}
	
}
