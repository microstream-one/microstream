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

import one.microstream.X;

public final class AggregateOffsetLength<E, R> implements Aggregator<E, R>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private long                           offset   ;
	private long                           length   ;
	private final Aggregator<? super E, R> aggregate;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AggregateOffsetLength(final long offset, final long length, final Aggregator<? super E, R> aggregate)
	{
		super();
		this.offset    = offset   ;
		this.length    = length   ;
		this.aggregate = aggregate;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E element)
	{
		if(this.offset > 0)
		{
			this.offset--;
			return;
		}
		this.aggregate.accept(element);
		if(--this.length == 0)
		{
			throw X.BREAK();
		}
	}

	@Override
	public final R yield()
	{
		return this.aggregate.yield();
	}

}
