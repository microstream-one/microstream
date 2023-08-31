package one.microstream.meta;

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

/**
 * Error that indicates that a piece of code (e.g. a method declared by an interface) should be, but is not implemented
 * yet.
 * <p>
 * This error has to be seen as a kind of "meta construct", because it should never appear or even be necessary to
 * exist in completed programs. Nevertheless, a construct like that is needed, because
 * software, even perfectly planned ones, are growing work pieces and as such need a way to indicate currently
 * existing construction sites. Even if it's only for testing and not for employing unfinished work-in-process software.
 * <p>
 * The current strategies of IDEs (or their default templates), to just return 0 / false / null and add a TO DO marker
 * is fine for the developer, but nothing than a serious, hardly runtime-traceable bug for testing / wip-software.<br>
 * The missing indicator error is currently substituted by an {@link UnsupportedOperationException}, which is
 * merely a workaround and abuse of an exception that is supposed to indicate something else (namely that the
 * requested procedure is not supported in a completed software).<br>
 * To resolve this workaround and do it properly, a not implemented exception has to exist.
 * <p>
 * Still, the goal of every (completed) software has to be to never use this class.
 *
 */
public class NotImplementedYetError extends Error
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public NotImplementedYetError()
	{
		super();
	}

	public NotImplementedYetError(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public NotImplementedYetError(final String message)
	{
		super(message);
	}

	public NotImplementedYetError(final Throwable cause)
	{
		super(cause);
	}

}
