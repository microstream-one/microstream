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

import static one.microstream.X.notNull;

import java.util.function.BiConsumer;

import one.microstream.persistence.exceptions.PersistenceExceptionConsistency;
import one.microstream.persistence.exceptions.PersistenceExceptionConsistencyUnknownTid;
import one.microstream.reference.Swizzling;


public interface PersistenceTypeManager extends PersistenceTypeRegistry
{
	public long ensureTypeId(Class<?> type);

	public Class<?> ensureType(long typeId);

	public long currentTypeId();

	public void updateCurrentHighestTypeId(long highestTypeId);

	
	
	public static PersistenceTypeManager.Default New(
		final PersistenceTypeRegistry   registry   ,
		final PersistenceTypeIdProvider tidProvider
	)
	{
		return new PersistenceTypeManager.Default(
			notNull(registry)   ,
			notNull(tidProvider)
		);
	}

	public final class Default implements PersistenceTypeManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceTypeRegistry   typeRegistry;
		final PersistenceTypeIdProvider tidProvider ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceTypeRegistry   registry   ,
			final PersistenceTypeIdProvider tidProvider
		)
		{
			super();
			this.typeRegistry = notNull(registry)   ;
			this.tidProvider  = notNull(tidProvider);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		protected long createNewTypeId()
		{
			return this.tidProvider.provideNextTypeId();
		}

		protected long internalEnsureTypeId(final Class<?> type)
		{
			long typeId;
			synchronized(this.typeRegistry)
			{
				// if not found either assign new oid or return the meanwhile registered oid
				if(Swizzling.isFoundId(typeId = this.typeRegistry.lookupTypeId(type)))
				{
					return typeId;
				}
				typeId = this.createNewTypeId();

				this.typeRegistry.registerType(typeId, type);
				if(type.getSuperclass() != null)
				{
					this.ensureTypeId(type.getSuperclass());
				}
			}
			return typeId;
		}

		@Override
		public long lookupTypeId(final Class<?> type)
		{
			return this.typeRegistry.lookupTypeId(type);
		}

		@Override
		public <T> Class<T> lookupType(final long tid)
		{
			return this.typeRegistry.lookupType(tid);
		}
		
		@Override
		public boolean validateTypeMapping(final long typeId, final Class<?> type) throws PersistenceExceptionConsistency
		{
			return this.typeRegistry.validateTypeMapping(typeId, type);
		}
		
		@Override
		public boolean validateTypeMappings(final Iterable<? extends PersistenceTypeLink> mappings)
			throws PersistenceExceptionConsistency
		{
			return this.typeRegistry.validateTypeMappings(mappings);
		}
		
		@Override
		public boolean registerTypes(final Iterable<? extends PersistenceTypeLink> types)
			throws PersistenceExceptionConsistency
		{
			return this.typeRegistry.registerTypes(types);
		}

		@Override
		public boolean registerType(final long tid, final Class<?> type)
		{
			return this.typeRegistry.registerType(tid, type);
		}

		@Override
		public long ensureTypeId(final Class<?> type)
		{
			final long typeId; // quick read-only check for already registered tid
			if(Swizzling.isFoundId(typeId = this.typeRegistry.lookupTypeId(type)))
			{
				// already present/found typeId is returned.
				return typeId;
			}
			
			// typeId not found, so a new typeId is ensured returned.
			return this.internalEnsureTypeId(type);
		}

		@Override
		public final long currentTypeId()
		{
			synchronized(this.typeRegistry)
			{
				return this.tidProvider.currentTypeId();
			}
		}

		@Override
		public void updateCurrentHighestTypeId(final long highestTypeId)
		{
			synchronized(this.typeRegistry)
			{
				final long currentTypeId = this.tidProvider.currentTypeId();
				if(currentTypeId > highestTypeId)
				{
					throw new IllegalArgumentException(
						"Current highest type id already passed desired new highest type id: "
						+ currentTypeId + " > " + highestTypeId
					);
				}
				this.tidProvider.updateCurrentTypeId(highestTypeId);
			}
		}
		
		@Override
		public Class<?> ensureType(final long typeId)
		{
			Class<?> type;
			if((type = this.typeRegistry.lookupType(typeId)) == null)
			{
				throw new PersistenceExceptionConsistencyUnknownTid(typeId);
			}
			return type;
		}
		
		@Override
		public void iteratePerIds(final BiConsumer<Long, ? super Class<?>> consumer)
		{
			this.typeRegistry.iteratePerIds(consumer);
		}
		
	}

}
