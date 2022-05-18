
package one.microstream.integrations.cdi.types;

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

/**
 * Defines the way the instance that will be stored in the {@link one.microstream.storage.types.StorageManager}.
 * <p>
 * E.g.: Given we have an Inventory class with a name and a list of products.
 * Lazy: will {@link one.microstream.storage.types.StorageManager#store} the list of products:
 * storageManager.store(inventory.getProducts());
 * <p>
 * EAGER: will {@link one.microstream.storage.types.StorageManager#store} the root instance:
 * storageManager.store(inventory);
 * To get more information: https://docs.microstream.one/manual/storage/storing-data/lazy-eager-full.html
 */
public enum StoreType
{
	/**
	 * Lazy storing is the default storing mode of the MicroStream engine.
	 * Referenced instances are stored only if they have not been stored yet.
	 * If a referenced instance has been stored previously it is not stored again even if it has been modified.
	 * It will use the method {@link one.microstream.storage.types.StorageManager#store}
	 * E.g.: The list of products: storageManager.store(inventory.getProducts());
	 */
	LAZY,
	/**
	 * In eager storing mode referenced instances are stored even if they had been stored before.
	 * Contrary to Lazy storing this will also store modified child objects at the cost of performance.
	 * It will {@link one.microstream.storage.types.StorageManager#createEagerStorer}.
	 * E.g.:
	 * Storer storer = storage.createEagerStorer();
	 * storer.store(inventory.getProducts());
	 * storer.commit();
	 */
	EAGER;
}
