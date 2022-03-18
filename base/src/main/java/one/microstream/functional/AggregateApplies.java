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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.function.Predicate;

import one.microstream.X;

public class AggregateApplies<E> implements Aggregator<E, Boolean>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private Boolean applies = TRUE;
	private final Predicate<? super E> predicate;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AggregateApplies(final Predicate<? super E> predicate)
	{
		super();
		this.predicate = predicate;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E element)
	{
		if(!this.predicate.test(element))
		{
			this.applies = FALSE;
			throw X.BREAK();
		}
	}

	@Override
	public final Boolean yield()
	{
		return this.applies;
	}

}
