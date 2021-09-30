package one.microstream.persistence.binary.jdk17.java.util;

/*-
 * #%L
 * MicroStream Persistence JDK17
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import java.util.List;

/**
 * Specialized handler for immutable List implementations in Java 15 and later
 * as found in java.util.ImmutableCollections.List12<E>
 *
 * the implementations are returned from List.of(), List.of(E e1) and  List.of(E e1, E e2)
 *
 * The handler takes the internal constant java.util.ImmutableCollections.EMPTY
 * into account which must not be persisted.
 */
public class BinaryHandlerImmutableCollectionsList12<T> extends AbstractBinaryHandlerGenericImmutableCollections12<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static BinaryHandlerImmutableCollectionsList12<?> New()
	{
		return new BinaryHandlerImmutableCollectionsList12<>(List.of(1).getClass());
	}


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected BinaryHandlerImmutableCollectionsList12(final Class<T> type)
	{
		super(type);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@SuppressWarnings("unchecked")
	@Override
	protected T createInstance(final Object e0, final Object e1)
	{
		if(e0 != null && e1 != null)
		{
			return (T) List.of(e0, e1);
		}
		if(e0 != null && e1 == null)
		{
			return (T) List.of(e0);
		}
		if(e0 == null && e1 != null)
		{
			return (T) List.of(e1);
		}

		return (T) List.of();
	}

}
