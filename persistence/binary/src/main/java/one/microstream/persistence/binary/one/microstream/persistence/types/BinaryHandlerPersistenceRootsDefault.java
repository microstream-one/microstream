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

import static one.microstream.X.notNull;

import one.microstream.chars.XChars;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceRootEntry;
import one.microstream.persistence.types.PersistenceRootResolver;
import one.microstream.persistence.types.PersistenceRootResolverProvider;
import one.microstream.persistence.types.PersistenceRoots;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.reference.Referencing;
import one.microstream.typing.KeyValue;


public final class BinaryHandlerPersistenceRootsDefault
extends AbstractBinaryHandlerCustom<PersistenceRoots.Default>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerPersistenceRootsDefault New(
		final PersistenceRootResolverProvider rootResolverProvider,
		final PersistenceObjectRegistry       globalRegistry
	)
	{
		return new BinaryHandlerPersistenceRootsDefault(
			notNull(rootResolverProvider),
			notNull(globalRegistry)
		);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceRootResolverProvider rootResolverProvider;

	/**
	 * The handler instance directly knowing the global registry might surprise at first and seem like a shortcut hack.
	 * However, when taking a closer look at the task of this handler: (globally) resolving global root instances,
	 * it becomes clear that direct access for registering resolved global instances at the global registry is
	 * indeed part of this handler's task.
	 */
	final PersistenceObjectRegistry globalRegistry;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerPersistenceRootsDefault(
		final PersistenceRootResolverProvider rootResolverProvider,
		final PersistenceObjectRegistry       globalRegistry
	)
	{
		super(
			PersistenceRoots.Default.class,
			CustomFields( // instances first to easy ref-only loading in storage
				Complex("instances",
					CustomField(Object.class, "instance")
				),
				Complex("identifiers",
					chars("identifier")
				)
			)
		);
		this.rootResolverProvider = rootResolverProvider;
		this.globalRegistry       = globalRegistry      ;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return true;
	}

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
	public final void store(
		final Binary                          data    ,
		final PersistenceRoots.Default        instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeRoots(this.typeId(), objectId, instance.entries(), handler);
	}

	@Override
	public final PersistenceRoots.Default create(final Binary data, final PersistenceLoadHandler handler)
	{
		// The identifier -> objectId root id mapping is created (and validated) from the loaded data.
		final EqHashTable<String, Long> rootIdMapping = data.buildRootMapping(EqHashTable.New());
		
		/* (10.12.2019 TM)TODO: PersistenceRoots constants instance oid association
		 * This method could collect all oids per identifier in the binary data and associate all
		 * linkable constants instances with their oid at the objectRegistry very easily and elegantly, here.
		 * Then there wouldn't be unnecessarily created instances that get discarded later on in update().
		 */
		return PersistenceRoots.Default.New(
			this.rootResolverProvider.provideRootResolver(),
			rootIdMapping
		);
	}
	
	@Override
	public final void updateState(
		final Binary                   data    ,
		final PersistenceRoots.Default instance,
		final PersistenceLoadHandler   handler
	)
	{
		final PersistenceRootResolver   rootResolver  = instance.$rootResolver ();
		final EqHashTable<String, Long> rootIdMapping = instance.$rootIdMapping();
		
		final EqHashTable<String, PersistenceRootEntry> resolvedRootEntries = EqHashTable.New();
		this.ensureRefactoredOldRoots(rootIdMapping, resolvedRootEntries, handler);

		// Root identifiers are resolved to root entries (with potentially mapped (= different) identifiers internally)
		rootResolver.resolveRootEntries(resolvedRootEntries, rootIdMapping.keys());
		
		// The entries are resolved to a mapping of current (= potentially mapped) identifiers to root instances.
		final XGettingTable<String, Object> resolvedRoots = rootResolver.resolveRootInstances(resolvedRootEntries);
		
		// The root instance's entries are updated (replaced) with the ones resolved in here.
		instance.loadingUpdateEntries(resolvedRoots);
		
		// The resolved instances need to be registered for their objectIds. Properly mapped to consider removed ones.
		this.registerInstancesPerObjectId(resolvedRootEntries, rootIdMapping);
	}
	
	/**
	 * @deprecated this method is as deprecated as the old root concept's identifier it uses.
	 * See {@link Persistence#customRootIdentifier()} and {@link Persistence#defaultRootIdentifier()}.
	 */
	@Deprecated
	private boolean ensureRefactoredOldRoots(
		final EqHashTable<String, Long>                 rootIdMapping,
		final EqHashTable<String, PersistenceRootEntry> resolvedRoots,
		final PersistenceLoadHandler                    handler
	)
	{
		final Long customRootOid  = rootIdMapping.get(Persistence.customRootIdentifier());
		final Long defaultRootOid = rootIdMapping.get(Persistence.defaultRootIdentifier());
		
		// quick check to abort for the non-refactoring (= normal) cases.
		if(customRootOid == null && defaultRootOid == null)
		{
			return false;
		}
		
		/*
		 * Reaching here means some refactoring has to be done. There are 4 cases to be covered.
		 * This is intentionally one big messy method to limit the deprecated conversion logic
		 * to one single method.
		 */

		final Object root = this.rootResolverProvider.rootReference().get();
		if(root == null)
		{
			// root refactoring case #1: root == null & customRoot exists
			if(customRootOid != null)
			{
				final Object customRoot = handler.lookupObject(customRootOid);
				if(customRoot == null)
				{
					throw new Error(
						"Root instance missing for identifier \"" + Persistence.customRootIdentifier() + "\""
					);
				}
				
				this.rootResolverProvider.rootReference().set(customRoot);
				resolvedRoots.add(Persistence.customRootIdentifier(), null);
				
				return true;
			}

			// root refactoring case #2: root == null & defaultRoot exists
			if(defaultRootOid != null)
			{
				final Object defaultRoot = handler.lookupObject(defaultRootOid);
				if(defaultRoot == null)
				{
					throw new Error(
						"Root instance missing for identifier \"" + Persistence.defaultRootIdentifier() + "\""
					);
				}
				if(!(defaultRoot instanceof Referencing<?>))
				{
					throw new Error(
						"Inconsistently typed default root instance: " + XChars.systemString(defaultRoot)
					);
				}
				
				final Referencing<?> casted = (Referencing<?>)defaultRoot;
				
				// safe as storing a root reference only stores the actual instance's objectId, not the supplier.
				this.rootResolverProvider.rootReference().setRootSupplier(() ->
					casted.get()
				);
				resolvedRoots.add(Persistence.defaultRootIdentifier(), null);
				
				return true;
			}
		}
		else
		{
			// root instance is not null, so it has to be associated with the existing objectIds

			// root refactoring case #3: root != null & customRoot exists
			if(customRootOid != null)
			{
				handler.registerCustomRootRefactoring(root, customRootOid);
				resolvedRoots.add(Persistence.customRootIdentifier(), null);
				
				return true;
			}

			// root refactoring case #4: root != null & defaultRoot exists
			if(defaultRootOid != null)
			{
				handler.registerDefaultRootRefactoring(root, defaultRootOid);
				resolvedRoots.add(Persistence.defaultRootIdentifier(), null);
				
				return true;
			}
		}
		
		// no refactoring case found
		return false;
	}

	private void registerInstancesPerObjectId(
		final XGettingTable<String, PersistenceRootEntry> resolvedRootEntries,
		final XGettingTable<String, Long>                 rootIdMapping
	)
	{
		final PersistenceObjectRegistry registry = this.globalRegistry;

		// lock the whole registry for the complete registration process because it might be used by other threads.
		synchronized(registry)
		{
			for(final KeyValue<String, PersistenceRootEntry> rootEntry : resolvedRootEntries)
			{
				if(rootEntry.value() == null)
				{
					// null-entries can (only) happen via automatic refactoring of old root types (custom/default).
					continue;
				}
				
				final Object rootInstance = rootEntry.value().instance();
				
				// instances can be null when either explicitly registered to be null in the refactoring or legacy enum
				if(rootInstance != null)
				{
					// must be the original identifier, not the potentially re-mapped identifier of the entry!
					final Long rootObjectId = rootIdMapping.get(rootEntry.key());
					
					// all live instances are registered for their OID.
					registry.registerConstant(rootObjectId.longValue(), rootInstance);
				}
			}
		}
	}
	
	@Override
	public final void complete(
		final Binary                   data    ,
		final PersistenceRoots.Default instance,
		final PersistenceLoadHandler   handler
	)
	{
		// temporary id mapping is no longer required
		instance.$discardRootIdMapping();
	}
		
	@Override
	public final void iterateInstanceReferences(
		final PersistenceRoots.Default instance,
		final PersistenceFunction      iterator
	)
	{
		// root identifiers are actually stored as an array of string values, not as string instances.
		for(final Object object : instance.entries().values())
		{
			iterator.apply(object);
		}
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		// the nice thing about this layout is: the references can be accessed directly as if it was a simple list
		data.iterateListElementReferences(0, iterator);
	}

}
