package one.microstream.util.cql;

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

import java.util.Comparator;
import java.util.function.Consumer;

import one.microstream.collections.sorting.SortableProcedure;
import one.microstream.collections.types.XIterable;
import one.microstream.functional.Aggregator;

public final class CqlWrapperCollectorSequenceSorting<O, R extends Consumer<O> & XIterable<O>>
implements Aggregator<O, R>
{
	final R target;
	final Comparator<? super O> order;

	CqlWrapperCollectorSequenceSorting(final R target, final Comparator<? super O> order)
	{
		super();
		this.target = target;
		this.order  = order ;
	}

	@Override
	public final void accept(final O element)
	{
		this.target.accept(element);
	}

	@Override
	public final R yield()
	{
		SortableProcedure.sortIfApplicable(this.target, this.order);
		return this.target;
	}

}
