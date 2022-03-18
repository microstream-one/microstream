package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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
 * (So far only a stub for the) conceptual type and ID consistency area.
 * All runtimes belonging to a specific domain must fulfill the following conditions:
 * - May not have different type definitions (but must not necessarily know all types of all other runtimes)
 * - May not assign object ids that collide with assignments from other runtimes
 *
 *
 * 
 */
public interface PersistenceDomain
{
	// empty stub so far
}
