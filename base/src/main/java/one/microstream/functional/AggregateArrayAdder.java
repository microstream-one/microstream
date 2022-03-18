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

public final class AggregateArrayAdder<E> implements Aggregator<E, Integer>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Predicate<? super E> predicate;
	private final E[] array;
	private int i;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AggregateArrayAdder(final Predicate<? super E> predicate, final E[] array, final int i)
	{
		super();
		this.predicate = predicate;
		this.array = array;
		this.i = i;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E e)
	{
		if(!this.predicate.test(e))
		{
			return;
		}
		this.array[this.i++] = e;
	}

	@Override
	public final Integer yield()
	{
		return this.i;
	}

}
