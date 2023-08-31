package one.microstream.persistence.binary.one.microstream.persistence.types;

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

import one.microstream.chars.XChars;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceException;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceRootReference;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerRootReferenceDefault extends AbstractBinaryHandlerCustom<PersistenceRootReference.Default>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	/**
	 * The handler instance directly knowing the global registry might surprise at first and seem like a shortcut hack.
	 * However, when taking a closer look at the task of this handler: (globally) resolving global root instances,
	 * it becomes clear that direct access for registering resolved global instances at the global registry is
	 * indeed part of this handler's task.
	 */
	final PersistenceRootReference.Default rootReference ;
	final PersistenceObjectRegistry        globalRegistry;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerRootReferenceDefault(
		final PersistenceRootReference.Default rootReference ,
		final PersistenceObjectRegistry        globalRegistry
		)
	{
		super(
			PersistenceRootReference.Default.class,
			CustomFields(
				CustomField(Object.class, "root")
			)
		);
		this.rootReference  = rootReference ;
		this.globalRegistry = globalRegistry;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}

	@Override
	public final boolean hasPersistedVariableLength()
	{
		return false;
	}

	static long getRootObjectId(final Binary data)
	{
		return data.read_long(0);
	}

	@Override
	public final void store(
		final Binary                           data    ,
		final PersistenceRootReference.Default instance,
		final long                             objectId,
		final PersistenceStoreHandler<Binary>  handler
		)
	{
		// root instance may even be null. Probably just temporarily to "truncate" a database or something like that.
		final Object rootInstance  = instance.get()             ;
		final long   contentLength = Binary.objectIdByteLength();
		final long   rootObjectId  = handler.apply(rootInstance);
		data.storeEntityHeader(contentLength, this.typeId(), objectId);
		data.store_long(rootObjectId);
	}

	@Override
	public final PersistenceRootReference.Default create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
		)
	{
		final Object rootInstance = this.rootReference.get();
		if(rootInstance != null)
		{
			/*
			 * If the singleton instance references a defined root object, it must be registered for the persisted
			 * objectId, even if the id references a record of different, incompatible, type. This conflict
			 * has to be recognized and reported in the corresponding type handler, but the defined root instance
			 * must have the persisted root object id associated in any case. Otherwise, there would be an
			 * inconsistency: a generic instance would be created for the persisted record and be generically
			 * registered with the persisted object id, thus leaving no object id for the actually defined root
			 * instance to be registered/associated with.
			 * If no rootInstance is defined, there is no such conflict. The generic instance of whatever type
			 * gets created and registered and can be queried by the application logic after initialization is
			 * complete.
			 */
			final long rootObjectId = getRootObjectId(data);
			handler.requireRoot(rootInstance, rootObjectId);
		}

		// instance is a singleton. Hence, no instance is created, here, but the singleton is returned.
		return this.rootReference;
	}

	@Override
	public final void updateState(
		final Binary                           data    ,
		final PersistenceRootReference.Default instance,
		final PersistenceLoadHandler           handler
	)
	{
		if(instance != this.rootReference)
		{
			throw new BinaryPersistenceException(
				"Initialized root reference and loaded root reference are not the same: initialized = "
				+ XChars.systemString(this.rootReference)
				+ " <-> loaded = "
				+ XChars.systemString(instance)
			);
		}
		
		final long   rootObjectId = getRootObjectId(data);
		final Object loadedRoot   = handler.lookupObject(rootObjectId);
		
		final Object rootInstance = this.rootReference.get();
		if(rootInstance == null)
		{
			/*
			 * If the instance has no explicit root instance set, a
			 * generically loaded and instantiated root instance is set.
			 */
			this.rootReference.setRoot(loadedRoot);

			return;
		}
		
		if(rootInstance == loadedRoot)
		{
			/*
			 * If referenced and loaded root are the same, everything is fine. No-op at this point.
			 * (#create should already have ensured that this case applies, but who knows ...)
			 */
			return;
		}
		
		throw new BinaryPersistenceException(
			"Initialized root instance and loaded root instance are not the same: initialized = "
			+ XChars.systemString(rootInstance)
			+ " <-> loaded = "
			+ XChars.systemString(loadedRoot)
		);
	}

	@Override
	public final void iterateInstanceReferences(
		final PersistenceRootReference.Default instance,
		final PersistenceFunction              iterator
		)
	{
		instance.iterate(iterator);
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		// trivial single-reference
		final long rootObjectId = getRootObjectId(data);
		
		// must require reference eagerly here as the call in #create did not create a build item.
		iterator.requireReferenceEager(rootObjectId);
	}
			
}
