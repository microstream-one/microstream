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

import java.util.function.Predicate;

import one.microstream.X;

public class AggregateSearch<E> implements Aggregator<E, E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private E found;
	private final Predicate<? super E> predicate;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AggregateSearch(final Predicate<? super E> predicate)
	{
		super();
		this.predicate = predicate;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void accept(final E element)
	{
		if(this.predicate.test(element))
		{
			this.found = element;
			throw X.BREAK();
		}
	}

	@Override
	public E yield()
	{
		return this.found;
	}

}
