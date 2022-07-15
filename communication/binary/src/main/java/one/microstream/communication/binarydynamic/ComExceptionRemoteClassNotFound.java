package one.microstream.communication.binarydynamic;

/*-
 * #%L
 * MicroStream Communication Binary
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

import one.microstream.com.ComException;

/**
 * Thrown when a typeDefinition received from the remote host
 * contains a type that can't be resolved to an exiting class
 * on the local system.
 *
 */
public class ComExceptionRemoteClassNotFound extends ComException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Constructs a <code>ComExceptionRemoteClassNotFound</code> with no detail message.
	 * 
	 * @param typeName the type name of the missing class
	 */
	public ComExceptionRemoteClassNotFound(final String typeName)
	{
		super("Class not found: " + typeName);
	}

}
