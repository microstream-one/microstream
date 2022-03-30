package one.microstream.persistence.binary.java.util;

/*-
 * #%L
 * microstream-persistence-binary
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

import java.util.WeakHashMap;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerWeakHashMap extends AbstractBinaryHandlerMap<WeakHashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<WeakHashMap<?, ?>> handledType()
	{
		return (Class)WeakHashMap.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerWeakHashMap New()
	{
		return new BinaryHandlerWeakHashMap();
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerWeakHashMap()
	{
		super(handledType());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final WeakHashMap<?, ?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new WeakHashMap<>();
	}

}
