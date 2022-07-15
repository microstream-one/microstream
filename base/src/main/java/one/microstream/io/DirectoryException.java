package one.microstream.io;

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

import java.io.File;


public class DirectoryException extends FileException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public DirectoryException(final File subject)
	{
		super(subject);
	}

	public DirectoryException(final File subject, final String message, final Throwable cause)
	{
		super(subject, message, cause);
	}

	public DirectoryException(final File subject, final String message)
	{
		super(subject, message);
	}

	public DirectoryException(final File subject, final Throwable cause)
	{
		super(subject, cause);
	}
	
}
