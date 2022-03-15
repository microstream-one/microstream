
package one.microstream.integrations.cdi.exceptions;

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

public class CDIExceptionStorage extends CDIException
{
	private static final String MESSAGE = "There is an incompatibility between the entity and the"
		+ " current root in the StorageManager. Please check the compatibility. "
		+ "Entity: %s and current root class %s";
	
	
	
	public <T, E> CDIExceptionStorage(final Class<T> entity, final Class<E> root)
	{
		super(String.format(MESSAGE, entity, root));
	}
}
