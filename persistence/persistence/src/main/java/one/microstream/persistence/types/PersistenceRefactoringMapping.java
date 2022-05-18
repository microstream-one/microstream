package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

import one.microstream.X;
import one.microstream.collections.EqConstHashEnum;
import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.collections.types.XImmutableTable;
import one.microstream.typing.KeyValue;

/**
 * A mapping that projects outdated identifiers (usually className#fieldName, but in case of root instances
 * also potentially arbitrary strings) to current identifiers.
 * 
 * 
 *
 */
public interface PersistenceRefactoringMapping
{
	public KeyValue<String, String> lookup(String key);
	
	public boolean isNewElement(String targetKey);
	

	
	public static PersistenceRefactoringMapping New()
	{
		return new Default(
			X.emptyTable(),
			X.empty()
		);
	}
	
	public static PersistenceRefactoringMapping New(
		final XGettingTable<String, String> entries
	)
	{
		return new Default(
			EqConstHashTable.New(entries),
			X.empty()
		);
	}
		
	public static PersistenceRefactoringMapping New(
		final XGettingTable<String, String> entries    ,
		final XGettingEnum<String>          newElements
	)
	{
		return new Default(
			EqConstHashTable.New(entries),
			EqConstHashEnum.New(newElements)
		);
	}
	
	public final class Default implements PersistenceRefactoringMapping
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XImmutableTable<String, String> entries    ;
		private final XImmutableEnum<String>          newElements;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final XImmutableTable<String, String> entries    ,
			final XImmutableEnum<String>          newElements
		)
		{
			super();
			this.entries     = entries    ;
			this.newElements = newElements;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final KeyValue<String, String> lookup(final String key)
		{
			return this.entries.lookup(key);
		}
		
		@Override
		public final boolean isNewElement(final String targetKey)
		{
			return this.newElements.contains(targetKey);
		}
		
	}
	
}
