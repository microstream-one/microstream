/*
 * Copyright (c) 2008-2010, Thomas Muenz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.jadoth.exceptions;

/**
 *
 * @author Thomas Muenz
 */
public class NotAnArrayException extends ClassCastException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final Class<?> wrongClass;
	private final Throwable cause;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

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
	// getters          //
	/////////////////////

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
