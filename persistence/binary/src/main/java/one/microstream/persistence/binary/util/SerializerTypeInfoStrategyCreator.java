package one.microstream.persistence.binary.util;

/*-
 * #%L
 * MicroStream Persistence Binary
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

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceManager;

public interface SerializerTypeInfoStrategyCreator
{
	public SerializerTypeInfoStrategy create(PersistenceManager<Binary> persistenceManager);
	
	public static class IncrementalDiff implements SerializerTypeInfoStrategyCreator
	{
		private final boolean includeTypeInfoOnce;
		
		public IncrementalDiff(final boolean includeTypeInfoOnce)
		{
			super();
			this.includeTypeInfoOnce = includeTypeInfoOnce;
		}

		@Override
		public SerializerTypeInfoStrategy create(final PersistenceManager<Binary> persistenceManager)
		{
			return new SerializerTypeInfoStrategy.IncrementalDiff(persistenceManager, this.includeTypeInfoOnce);
		}
	}
	
	public static class Diff implements SerializerTypeInfoStrategyCreator
	{
		private final boolean includeTypeInfoOnce;
		
		public Diff(final boolean includeTypeInfoOnce)
		{
			super();
			this.includeTypeInfoOnce = includeTypeInfoOnce;
		}
		
		@Override
		public SerializerTypeInfoStrategy create(final PersistenceManager<Binary> persistenceManager)
		{
			return new SerializerTypeInfoStrategy.Diff(persistenceManager, this.includeTypeInfoOnce);
		}
	}
	
	public static class TypeDictionary implements SerializerTypeInfoStrategyCreator
	{
		private final boolean includeTypeInfoOnce;
		
		public TypeDictionary(final boolean includeTypeInfoOnce)
		{
			super();
			this.includeTypeInfoOnce = includeTypeInfoOnce;
		}
			
		@Override
		public SerializerTypeInfoStrategy create(final PersistenceManager<Binary> persistenceManager)
		{
			return new SerializerTypeInfoStrategy.TypeDictionary(persistenceManager, this.includeTypeInfoOnce);
		}
	}
}
