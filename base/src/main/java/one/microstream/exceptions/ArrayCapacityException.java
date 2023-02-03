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

/**
 * This implementation intentionally does NOT extend from {@link ArrayIndexOutOfBoundsException}.
 * See architectural explanation in {@link IndexBoundsException}.
 *
 */
// hopefully, this can be removed at some point in the future ...
public class ArrayCapacityException extends IndexBoundsException
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final String MESSAGE_BODY = "Java technical array capacity limit of max signed 32 bit integer value exceeded";
	


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ArrayCapacityException()
	{
		this(Integer.MAX_VALUE + 1L);
	}

	public ArrayCapacityException(final long exceedingCapacity)
	{
		this(exceedingCapacity, null);
	}

	public ArrayCapacityException(final long exceedingCapacity, final String s)
	{
		super(Integer.MAX_VALUE , exceedingCapacity, s);
	}

	@Override
	public String assembleDetailString()
	{
		return MESSAGE_BODY + ": " + this.index();
	}

	
	
	// hacky buggy security hole misconception serialization
	private static final long serialVersionUID = 3168758028720258369L;
}
