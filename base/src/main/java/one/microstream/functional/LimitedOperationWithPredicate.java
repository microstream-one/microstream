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

import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.branching.ThrowBreak;

public final class LimitedOperationWithPredicate<E> implements Consumer<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private int skip;
	private int lim;
	private final Predicate<? super E> predicate;
	private final Consumer<? super E> procedure;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public LimitedOperationWithPredicate(
		final int skip,
		final int limit,
		final Predicate<? super E> predicate,
		final Consumer<? super E> procedure
	)
	{
		super();
		this.skip = skip;
		this.lim = limit;
		this.predicate = predicate;
		this.procedure = procedure;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E e)
	{
		try
		{
			if(!this.predicate.test(e))
			{
				return;
			}
			if(this.skip > 0)
			{
				this.skip--;
				return;
			}
			this.procedure.accept(e);
			if(--this.lim == 0)
			{
				throw X.BREAK();
			}
		}
		catch(final ThrowBreak t)
		{
			throw t;
		}
	}

}
