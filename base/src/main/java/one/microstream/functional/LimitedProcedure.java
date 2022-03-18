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

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.X;


public final class LimitedProcedure<E> implements Consumer<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Consumer<? super E> procedure;
	private int skip;
	private int limit;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public LimitedProcedure(final Consumer<? super E> procedure, final int skip, final int limit)
	{
		super();
		this.procedure = notNull(procedure);
		this.skip      = skip;
		this.limit     = limit;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E e)
	{
		if(this.skip > 0)
		{
			this.skip--;
			return;
		}
		if(this.limit > 0)
		{
			this.procedure.accept(e);
			this.limit--; // decrement after procedure call to let continue throw skip it? tricky... maybe too much
			return;
		}
		throw X.BREAK();
	}

}
