
package one.microstream.integrations.cdi.types.extension;

/*-
 * #%L
 * microstream-integrations-cdi
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

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import one.microstream.integrations.cdi.types.Store;
import one.microstream.persistence.types.PersistenceStoring;
import one.microstream.persistence.types.Storer;
import one.microstream.storage.types.StorageManager;


enum StoreTypeStrategy implements StoreStrategy
{
	EAGER
	{
		@Override
		public void store(final Store store, final StorageManager manager, final StorageExtension extension)
		{
			LOGGER.log(Level.WARNING, "Store with Eager has a high cost of performance.");
			final Object root   = manager.root();
			final Storer storer = manager.createEagerStorer();
			execute(store, extension, root, storer);
			storer.commit();
		}
	},
	
	LAZY
	{
		@Override
		public void store(final Store store, final StorageManager manager, final StorageExtension extension)
		{
			final Object root = manager.root();
			execute(store, extension, root, manager);
		}
	};
	
	private static final Logger LOGGER = Logger.getLogger(StoreTypeStrategy.class.getName());
	
	private static void execute(
		final Store              store    ,
		final StorageExtension   extension,
		final Object             root     ,
		final PersistenceStoring storing
	)
	{
		if(store.root())
		{
			storeRoot(root, storing);
		}
		else
		{
			final Optional<EntityMetadata> metadata = extension.get(root.getClass());
			if(metadata.isEmpty())
			{
				LOGGER.log(
					Level.FINEST,
					"There is no entity with the @Storage annotation to the current root class "
						+
						root.getClass()
						+ " so it will store by root"
				);
				storeRoot(root, storing);
			}
			metadata.ifPresent(m -> m.values(root, store.fields()).forEach(storing::store));
			LOGGER.log(
				Level.FINEST,
				"Storing Iterables and Maps fields from the root class "
					+ root.getClass()
					+ " the fields: "
					+ store.fields()
			);
		}
	}
	
	private static void storeRoot(final Object root, final PersistenceStoring storing)
	{
		final long storeId = storing.store(root);
		LOGGER.log(Level.WARNING, "Store the root it might return performance issue " + storeId);
	}
	
}
