package one.microstream.persistence.binary.one.microstream.util;

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

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.one.microstream.collections.BinaryHandlerEqHashEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.util.Substituter;


public final class BinaryHandlerSubstituterDefault
extends AbstractBinaryHandlerCustom<Substituter.Default<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<Substituter.Default<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)Substituter.Default.class;
	}
	
	public static BinaryHandlerSubstituterDefault New()
	{
		return new BinaryHandlerSubstituterDefault();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerSubstituterDefault()
	{
		// binary layout definition
		super(
			handledType(),
			BinaryHandlerEqHashEnum.Fields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}
	
	@Override
	public final boolean hasPersistedVariableLength()
	{
		return true;
	}

	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return true;
	}

	@Override
	public final void store(
		final Binary                          data    ,
		final Substituter.Default<?>          instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		synchronized(instance)
		{
			BinaryHandlerEqHashEnum.staticStore(data, instance.$elements(), this.typeId(), objectId, handler);
		}
	}

	@Override
	public final Substituter.Default<?> create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		// hashEqualator gets set in update
		return Substituter.New(BinaryHandlerEqHashEnum.staticCreate(data));
	}

	@Override
	public final void updateState(
		final Binary                 data    ,
		final Substituter.Default<?> instance,
		final PersistenceLoadHandler handler
	)
	{
		synchronized(instance)
		{
			BinaryHandlerEqHashEnum.staticUpdate(data, instance.$elements(), handler);
		}
	}

	@Override
	public void complete(
		final Binary                 data    ,
		final Substituter.Default<?> instance,
		final PersistenceLoadHandler handler
	)
	{
		synchronized(instance)
		{
			BinaryHandlerEqHashEnum.staticComplete(data, instance.$elements());
		}
	}

	@Override
	public final void iterateLoadableReferences(
		final Binary                     data    ,
		final PersistenceReferenceLoader iterator
	)
	{
		BinaryHandlerEqHashEnum.staticIteratePersistedReferences(data, iterator);
	}

}
