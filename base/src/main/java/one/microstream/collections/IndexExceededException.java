package one.microstream.collections;

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

import one.microstream.chars.VarString;

public class IndexExceededException extends IndexOutOfBoundsException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long bound;
	private final long index;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public IndexExceededException(final long bound, final long index)
	{
		super();
		this.bound = bound;
		this.index = index;
	}

	public IndexExceededException(final long bound, final long index, final String message)
	{
		super(message);
		this.bound = bound;
		this.index = index;
	}

	public IndexExceededException()
	{
		this(0, 0);
	}

	public IndexExceededException(final String message)
	{
		this(0, 0, message);
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public long index()
	{
		return this.index;
	}

	public long bound()
	{
		return this.bound;
	}

	@Override
	public String getMessage()
	{
		final VarString vc = VarString.New()
			.add("Index = ").add(this.index).add(", bound = ").add(this.bound).add('.')
		;
		final String message = super.getMessage();
		if(message != null)
		{
			vc.add(' ').add(message);
		}
		return vc.toString();
	}



}
