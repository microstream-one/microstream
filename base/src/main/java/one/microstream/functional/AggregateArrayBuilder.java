package one.microstream.functional;

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

import one.microstream.collections.BulkList;

public final class AggregateArrayBuilder<E> implements Aggregator<E, E[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final AggregateArrayBuilder<Object> New()
	{
		return New(Object.class);
	}

	public static final <E> AggregateArrayBuilder<E> New(final Class<E> elementType)
	{
		return New(elementType, 1);
	}

	public static final <E> AggregateArrayBuilder<E> New(final Class<E> elementType, final int initialCapacity)
	{
		return new AggregateArrayBuilder<>(elementType, new BulkList<E>(initialCapacity));
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Class<E>    elementType;
	final BulkList<E> collector  ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	AggregateArrayBuilder(final Class<E> elementType, final BulkList<E> collector)
	{
		super();
		this.elementType = elementType;
		this.collector   = collector  ;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E element)
	{
		this.collector.add(element);
	}

	@Override
	public final E[] yield()
	{
		return this.collector.toArray(this.elementType);
	}

}
