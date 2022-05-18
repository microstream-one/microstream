package one.microstream.branching;

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
 * Thrown to signals the outer context to break the current loop,
 * normally proceeding with the actions following the loop.
 */
public class ThrowBreak extends AbstractBranchingThrow
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ThrowBreak()
	{
		super();
	}

	public ThrowBreak(final Throwable cause)
	{
		super(cause);
	}

	public ThrowBreak(final Object hint)
	{
		super(hint);
	}

	public ThrowBreak(final Object hint, final Throwable cause)
	{
		super(hint, cause);
	}



}
