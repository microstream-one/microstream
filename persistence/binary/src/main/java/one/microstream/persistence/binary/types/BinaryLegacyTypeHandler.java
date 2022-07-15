package one.microstream.persistence.binary.types;

/*-
 * #%L
 * microstream-persistence-binary
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

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.types.PersistenceLegacyTypeHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

public interface BinaryLegacyTypeHandler<T> extends PersistenceLegacyTypeHandler<Binary, T>, BinaryTypeHandler<T>
{
	@Override
	public default BinaryLegacyTypeHandler<T> initialize(final long typeId)
	{
		PersistenceLegacyTypeHandler.super.initialize(typeId);
		return this;
	}
		
	@Override
	public default void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		PersistenceLegacyTypeHandler.super.store(data, instance, objectId, handler);
	}
	
	
	
	public abstract class Abstract<T>
	extends PersistenceLegacyTypeHandler.Abstract<Binary, T>
	implements BinaryLegacyTypeHandler<T>
	{
		protected Abstract(final PersistenceTypeDefinition typeDefinition)
		{
			super(typeDefinition);
		}
		
	}
	
	public abstract class AbstractCustom<T>
	extends AbstractBinaryHandlerCustom<T>
	implements BinaryLegacyTypeHandler<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected AbstractCustom(
			final Class<T>                                                    type   ,
			final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
		)
		{
			super(type, members);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public synchronized BinaryLegacyTypeHandler.AbstractCustom<T> initialize(final long typeId)
		{
			super.initialize(typeId);
			return this;
		}
		
		@Override
		public void store(
			final Binary                          data    ,
			final T                               instance,
			final long                            objectId,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			BinaryLegacyTypeHandler.super.store(data, instance, objectId, handler);
		}
		
	}
	
}
