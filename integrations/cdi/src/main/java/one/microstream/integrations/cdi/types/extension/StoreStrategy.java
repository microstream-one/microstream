
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

import java.util.Objects;

import one.microstream.integrations.cdi.types.Store;
import one.microstream.integrations.cdi.types.StoreType;
import one.microstream.storage.types.StorageManager;


interface StoreStrategy
{
	void store(Store store, StorageManager manager, StorageExtension extension);
	
	static StoreStrategy of(final Store store)
	{
		Objects.requireNonNull(store, "store is required");
		return StoreType.EAGER.equals(store.value())
			? StoreTypeStrategy.EAGER
			: StoreTypeStrategy.LAZY
		;
	}
}
