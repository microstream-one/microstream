package one.microstream.collections.interfaces;

/*-
 * #%L
 * microstream-base
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

import java.lang.ref.WeakReference;

public interface ConsolidatableCollection
{
	/**
	 * Consolidates the internal storage of this collection by discarding all elements of the internal storage that
	 * have become obsolete or otherwise unneeded anymore. (e.g. {@link WeakReference} entries whose reference has
	 * been cleared).
	 * If this is not possible or not needed in the concrete implementation, this method does nothing and returns 0.
	 * @return the number of discarded entries.
	 */
	public long consolidate();
}
