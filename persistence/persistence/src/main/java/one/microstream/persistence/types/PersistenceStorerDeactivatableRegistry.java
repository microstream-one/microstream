package one.microstream.persistence.types;

/*-
 * #%L
 * MicroStream Persistence
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

public interface PersistenceStorerDeactivatableRegistry
{
	/**
	 * Register a PersistenceStorerDeactivatable instance to the StorerModeController.
	 * 
	 * @param deactivatableStorer the PersistenceStorerDeactivatable to be registered.
	 * 
	 * @return the registered instance.
	 */
	public PersistenceStorerDeactivatable register(PersistenceStorerDeactivatable deactivatableStorer);

	/**
	 * Returns true if there are any registered PersistenceStorerDeactivatable instances.
	 * 
	 * @return true or false.
	 */
	public boolean hasRegisteredStorers();

	/**
	 * Cleanup all no more valid (garbage collected) Storer instances.
	 */
	public void clean();
}
