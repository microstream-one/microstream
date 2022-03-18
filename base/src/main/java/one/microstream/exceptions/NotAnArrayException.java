package one.microstream.exceptions;

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

public class NotAnArrayException extends ClassCastException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Class<?> wrongClass;
	private final Throwable cause;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public NotAnArrayException()
	{
		super();
		this.wrongClass = null;
		this.cause = null;
	}

	public NotAnArrayException(final String message, final Throwable cause)
	{
		super(message);
		this.wrongClass = null;
		this.cause = cause;
	}

	public NotAnArrayException(final String message)
	{
		super(message);
		this.wrongClass = null;
		this.cause = null;
	}

	public NotAnArrayException(final Throwable cause)
	{
		super();
		this.wrongClass = null;
		this.cause = cause;
	}

	public NotAnArrayException(final Class<?> wrongClass)
	{
		super();
		this.wrongClass = wrongClass;
		this.cause = null;
	}

	public NotAnArrayException(final Class<?> wrongClass, final Throwable cause)
	{
		super();
		this.wrongClass = wrongClass;
		this.cause = cause;
	}

	public NotAnArrayException(final Class<?> wrongClass, final String message)
	{
		super(message);
		this.wrongClass = wrongClass;
		this.cause = null;
	}

	public NotAnArrayException(final Class<?> wrongClass, final String message, final Throwable cause)
	{
		super(message);
		this.wrongClass = wrongClass;
		this.cause = cause;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public Class<?> getWrongClass()
	{
		return this.wrongClass;
	}

	@Override
	public synchronized Throwable getCause()
	{
		return this.cause;
	}

	@Override
	public String getMessage()
	{
		return "Wrong Class: " + this.wrongClass.getName();
	}

}
