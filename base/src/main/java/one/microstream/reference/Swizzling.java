package one.microstream.reference;

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

public final class Swizzling
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final long nullId()
	{
		return 0L;
	}
	
	public static final long notFoundId()
	{
		return -1L;
	}
	
	public static final boolean isNullId(final long objectId)
	{
		return objectId == nullId();
	}
	
	public static final boolean isFoundId(final long objectId)
	{
		return objectId >= nullId();
	}
	
	public static final boolean isNotFoundId(final long objectId)
	{
		return objectId < nullId();
	}
	
	public static final boolean isProperId(final long objectId)
	{
		return objectId > nullId();
	}
	
	public static final boolean isNotProperId(final long objectId)
	{
		return objectId <= nullId();
	}
	
	public static final long toUnmappedObjectId(final Object object)
	{
		return object == null
			? nullId()
			: notFoundId()
		;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private Swizzling()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
