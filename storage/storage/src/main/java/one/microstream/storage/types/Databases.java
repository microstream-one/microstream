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

import one.microstream.collections.EqHashTable;

public interface Databases
{
	public Database get(String databaseName);
		
	public Database ensureStoragelessDatabase(String databaseName);
	
	
	
	public static Databases get()
	{
		return Static.get();
	}

	public final class Static
	{
		private static final Databases SINGLETON = Databases.New();
		
		static Databases get()
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
		public final synchronized Database get(final String databaseName)
		{
			return this.databases.get(databaseName);
		}

		@Override
		public final synchronized Database ensureStoragelessDatabase(final String databaseName)
		{
			Database database = this.get(databaseName);
			if(database != null)
			{
				database.guaranteeNoActiveStorage();
			}
			else
			{
				database = Database.New(databaseName);
				this.databases.add(databaseName, database);
			}
			
			return database;
		}
		
	}
	
}
