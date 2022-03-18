package one.microstream.persistence.binary.java.util.concurrent;

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

import java.util.concurrent.ConcurrentHashMap;

import one.microstream.X;
import one.microstream.persistence.binary.java.util.AbstractBinaryHandlerMap;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerConcurrentHashMap extends AbstractBinaryHandlerMap<ConcurrentHashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ConcurrentHashMap<?, ?>> handledType()
	{
		return (Class)ConcurrentHashMap.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerConcurrentHashMap New()
	{
		return new BinaryHandlerConcurrentHashMap();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerConcurrentHashMap()
	{
		super(
			handledType()
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public ConcurrentHashMap<?, ?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new ConcurrentHashMap<>(
			X.checkArrayRange(getElementCount(data))
		);
	}
	
}
