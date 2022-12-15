package one.microstream.persistence.util;

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

import java.util.function.Consumer;

import one.microstream.persistence.types.PersistenceLoader;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.reference.Swizzling;
import one.microstream.util.traversing.ObjectGraphTraverser;

/**
 * Utility to reload objects or object graphs.
 * Reload means that the in-memory state of the objects is reset to the state of the underlying storage.
 * <p>
 * Usage:
 * <pre>
 * EmbeddedStorageManager storage = ...;
 * Reloader reloader = Reloader.New(storage.persistenceManager());
 * reloader.reloadFlat(obj); // reloads only the given object, but not its references
 * reloader.reloadDeep(obj); // reloads the complete object graph
 * </pre>
 * 
 * @since 08.00.00
 */
public interface Reloader
{
	/**
	 * Resets the state of the given instance to the state of the underlying storage. But not its references.
	 * 
	 * @param <T> type of the instance
	 * @param instance the object to reload
	 * @return the reloaded object, or <code>null</code> if it was not found in the storage
	 */
	public <T> T reloadFlat(T instance);
	
	/**
	 * Resets the state of the given instance and all of its references to the state of the underlying storage.
	 * 
	 * @param <T> type of the instance
	 * @param instance the object to reload
	 * @return the reloaded object, or <code>null</code> if it was not found in the storage
	 */
	public <T> T reloadDeep(T instance);
	
	
	/**
	 * Pseudo-constructor method to create a new {@link Reloader}.
	 * 
	 * @param persistenceManager the persistence manager to reload from
	 * @return a newly created {@link Reloader}
	 */
	public static Reloader New(final PersistenceManager<?> persistenceManager)
	{
		return new Reloader.Default(
			notNull(persistenceManager)
		);
	}
	
	
	public static class Default implements Reloader
	{
		private final PersistenceManager<?> persistenceManager;

		Default(
			final PersistenceManager<?> persistenceManager
		)
		{
			super();
			this.persistenceManager = persistenceManager;
		}
		
		private Object reloadObject(
			final Object            instance,
			final PersistenceLoader loader
		)
		{
			final long oid;
			return Swizzling.isFoundId(oid = this.persistenceManager.lookupObjectId(instance))
				? loader.getObject(oid)
				: null
			;
		}
		
		@SuppressWarnings("unchecked") // type safety ensured by logic
		@Override
		public <T> T reloadFlat(final T instance)
		{
			notNull(instance);
			
			return (T)this.reloadObject(
				instance,
				this.persistenceManager.createLoader()
			);
		}

		@SuppressWarnings("unchecked") // type safety ensured by logic
		@Override
		public <T> T reloadDeep(final T instance)
		{
			notNull(instance);
			
			final long oid;
			if(Swizzling.isNotFoundId(oid = this.persistenceManager.lookupObjectId(instance)))
			{
				return null;
			}
			
			final PersistenceLoader loader = this.persistenceManager.createLoader();
			final Consumer<Object>  logic  = object -> this.reloadObject(object, loader);
			
			// reload references
			ObjectGraphTraverser.Builder()
				.modeFull()
				.acceptorLogic(logic)
				.buildObjectGraphTraverser()
				.traverse(instance)
			;
			
			// reload instance
			return (T)loader.getObject(oid);
		}
		
	}
	
}
