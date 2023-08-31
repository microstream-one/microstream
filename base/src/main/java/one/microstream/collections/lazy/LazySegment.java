package one.microstream.collections.lazy;

/*-
 * #%L
 * MicroStream Base
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

public interface LazySegment<E> {

	/**
	 * get the number of contained element in this segment.
	 * 
	 * @return number of contained elements;
	 */
	int size();
	
	/**
	 * Check if the lazy loaded data of this segment has been loaded.
	 * 
	 * @return true if loaded, otherwise false.
	 */
	boolean isLoaded();

	/**
	 * Check if this segment has modifications that are not yet persisted.
	 * 
	 * @return true if there are modifications not yet persisted, otherwise false.
	 */
	boolean isModified();

	/**
	 * Unload the lazy data of this segment
	 */
	void unloadSegment();

	boolean unloadAllowed();

	void allowUnload(final boolean allow);

	E getData();

}
