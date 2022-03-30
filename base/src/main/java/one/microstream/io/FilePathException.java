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

import java.nio.file.Path;


public class FilePathException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Path subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public FilePathException(final Path subject)
	{
		super();
		this.subject = subject;
	}

	public FilePathException(final Path subject, final String message, final Throwable cause)
	{
		super(message, cause);
		this.subject = subject;
	}

	public FilePathException(final Path subject, final String message)
	{
		super(message);
		this.subject = subject;
	}

	public FilePathException(final Path subject, final Throwable cause)
	{
		super(cause);
		this.subject = subject;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Path getSubject()
	{
		return this.subject;
	}
	
	@Override
	public String getMessage()
	{
		return super.getMessage() + " " + this.subject;
	}

}
