package one.microstream.storage.restadapter.types;

/*-
 * #%L
 * microstream-storage-restadapter
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

import java.util.function.Consumer;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;

public class ViewerBinaryTypeHandlerGeneric implements PersistenceTypeHandler<Binary, ObjectDescription>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final ValueReader[] readers;
	private final PersistenceTypeDefinition persitenceTypeDefinition;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerBinaryTypeHandlerGeneric(final PersistenceTypeDefinition persistenceTypeDef)
	{
		super();

		this.persitenceTypeDefinition = persistenceTypeDef;
		this.readers = ValueReader.deriveValueReaders(persistenceTypeDef);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public PersistenceTypeDefinition getPersitenceTypeDefinition()
	{
		return this.persitenceTypeDefinition;
	}

	@Override
	public long typeId()
	{
		return this.persitenceTypeDefinition.typeId();
	}

	@Override
	public String typeName()
	{
		return this.persitenceTypeDefinition.typeName();
	}

	@Override
	public boolean hasPersistedReferences()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long membersPersistedLengthMinimum()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long membersPersistedLengthMaximum()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isPrimitiveType()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<ObjectDescription> type()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void iterateInstanceReferences(final ObjectDescription instance, final PersistenceFunction iterator)
	{
		throw new UnsupportedOperationException();

	}

	@Override
	public void iterateLoadableReferences(final Binary medium, final PersistenceReferenceLoader iterator)
	{
		//do nothing
	}

	@Override
	public void store(
		final Binary                          data    ,
		final ObjectDescription               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ObjectDescription create(final Binary medium, final PersistenceLoadHandler handler)
	{
		final ObjectDescription objectDescription = new ObjectDescription();
		objectDescription.setObjectId(medium.getBuildItemObjectId());
		objectDescription.setPersistenceTypeDefinition(this.persitenceTypeDefinition);

		ValueReader.readObjectValues(medium, this.readers, null, objectDescription);

		return	objectDescription;
	}

	@Override
	public void updateState(final Binary medium, final ObjectDescription instance, final PersistenceLoadHandler handler)
	{
		//do nothing
	}

	@Override
	public void complete(final Binary medium, final ObjectDescription instance, final PersistenceLoadHandler handler)
	{
		//do nothing
	}

	@Override
	public PersistenceTypeHandler<Binary, ObjectDescription> initialize(final long typeId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<Binary> dataType()
	{
		throw new UnsupportedOperationException();
	}
}
