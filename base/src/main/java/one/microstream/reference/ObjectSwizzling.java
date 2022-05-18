package one.microstream.reference;

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

public interface ObjectSwizzling
{
	/**
	 * Retrieves the instance associated with the passed {@literal objectId}. Retrieving means guaranteeing that
	 * the associated instance is returned. If it does not yet exist, it will be created from persisted data,
	 * including all non-lazily referenced objects it is connected to.
	 * 
	 * @param objectId the {@literal objectId} defining which instance to return.
	 * 
	 * @return the instance associated with the passed {@literal objectId}.
	 */
	public Object getObject(long objectId);
}
