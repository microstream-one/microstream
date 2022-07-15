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
	
	/**
	 * A Creator for a {@link SerializerTypeInfoStrategy}.
	 * The created SerializerTypeInfoStrategy includes only type information
	 * for types added to the serializers type registry in the current serialization.
	 * Types that are registered during the serializers setup are never included.
	 */
	public static class IncrementalDiff implements SerializerTypeInfoStrategyCreator
	{
		private final boolean includeTypeInfoOnce;
		
		/**
		 * Construct a creator for a {@link SerializerTypeInfoStrategy}.
		 * The created SerializerTypeInfoStrategy includes only type information
		 * for types added to the serializers type registry in the current serialization.
		 * Types that are registered during the serializers setup are never included.
		 * 
		 * @param includeTypeInfoOnce If true, the new type information is included
		 * only once in the serialization pass that has the new type.
		 * Subsequent serialization will not contain type information if no new type was serialized.
		 */
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
	
	/**
	 * A Creator for a {@link SerializerTypeInfoStrategy}.
	 * The created SerializerTypeInfoStrategy includes type information
	 * for types added to the serializers type registry in the current serialization
	 * and all previous serializations.
	 * Types that are registered during the serializers setup are never included.
	 */
	public static class Diff implements SerializerTypeInfoStrategyCreator
	{
		private final boolean includeTypeInfoOnce;
		
		/**
		 * Construct a Creator for a {@link SerializerTypeInfoStrategy}.
		 * The created SerializerTypeInfoStrategy includes type information
		 * for types added to the serializers type registry in the current serialization
		 * and all previous serializations.
		 * Types that are registered during the serializers setup are never included.
		 *
		 * @param includeTypeInfoOnce If true, the new type information is included
		 * only once in the serialization pass that has the new type.
		 * Subsequent serialization will not contain type information if no new type was serialized.
		 */
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
	
	/**
	 * A Creator for a {@link SerializerTypeInfoStrategy}.
	 * The created SerializerTypeInfoStrategy includes type information
	 * for all types currently known to the serializer including those registered during the setup.
	 *
	 */
	public static class TypeDictionary implements SerializerTypeInfoStrategyCreator
	{
		private final boolean includeTypeInfoOnce;
		
		/**
		 * A Creator for a {@link SerializerTypeInfoStrategy}.
		 * The created SerializerTypeInfoStrategy includes type information
		 * for all types currently known to the serializer including those registered during the setup.
		 * 
		 * @param includeTypeInfoOnce If true, the type information is included
		 * only in the serialization pass that has the new types.
		 * Subsequent serialization will not contain type information if no new type was serialized.
		 */
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
