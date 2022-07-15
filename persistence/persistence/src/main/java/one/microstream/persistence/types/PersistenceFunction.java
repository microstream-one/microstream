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

public interface PersistenceFunction
{
	/**
	 * Applies any action on the passed instance (e.g.: simply looking up its object ID or
	 * storing its state to a storage medium) and returns the object ID that identifies the passed instance.
	 * The returned OID may be the existing one for the passed instance or a newly associated one.
	 * 
	 * @param <T> the instance's type
	 * @param instance the instance to which the function shall be applied.
	 * @return the object ID (OID) that is associated with the passed instance.
	 */
	public <T> long apply(T instance);

}
