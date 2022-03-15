
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




/**
 * Get the exception when a class has a no arg constructor either public or default
 */
public class CDIExceptionMissingDefaultConstructor extends CDIException
{
	public <T> CDIExceptionMissingDefaultConstructor(final Class<T> clazz)
	{
		super("This class must have a no arg constructor with either public and default visibility: " + clazz.getName());
	}
}
