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
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.collections.types.XIterable;
import one.microstream.collections.types.XSequence;

public interface CqlTransfer<I, R extends XIterable<I>> extends CqlIteration<I, I, R>
{
	@Override
	public default CqlTransfer<I, R> skip(final Number count)
	{
		return CqlTransfer.New(
			this.getSource()  ,
			CQL.asLong(count),
			this.getLimit()   ,
			this.getSelector(),
			this.getOrder()   ,
			this.getResultor()
		);
	}

	@Override
	public default CqlTransfer<I, R> limit(final Number count)
	{
		return CqlTransfer.New(
			this.getSource()  ,
			this.getSkip()    ,
			CQL.asLong(count),
			this.getSelector(),
			this.getOrder()   ,
			this.getResultor()
		);
	}

	@Override
	public default CqlTransfer<I, R> from(final XIterable<? extends I> source)
	{
		return CqlTransfer.New(
			source            ,
			this.getSkip()    ,
			this.getLimit()   ,
			this.getSelector(),
			this.getOrder()   ,
			this.getResultor()
		);
	}

	@Override
	public default CqlTransfer<I, R> orderBy(final Comparator<? super I> order)
	{
		return CqlTransfer.New(
			this.getSource()  ,
			this.getSkip()    ,
			this.getLimit()   ,
			this.getSelector(),
			order             ,
			this.getResultor()
		);
	}

	@Override
	public default CqlTransfer<I, R> select(final Predicate<? super I> selector)
	{
		return CqlTransfer.New(
			this.getSource()  ,
			this.getSkip()    ,
			this.getLimit()   ,
			selector          ,
			this.getOrder()   ,
			this.getResultor()
		);
	}

	@Override
	public default R execute()
	{
		return this.executeOn(CQL.prepareSource(this.getSource()));
	}

	@Override
	public default <P extends Consumer<? super I>> P iterate(final P procedure)
	{
		this.execute().iterate(procedure);
		return procedure;
	}

	@Override
	public default R executeOn(final XIterable<? extends I> source)
	{
		return CQL.executeQuery(
			source            ,
			this.getSkip()    ,
			this.getLimit()   ,
			this.getSelector(),
			this.getResultor(),
			this.getOrder()
		);
	}

	@Override
	public default <P extends Consumer<I>>P executeInto(final XIterable<? extends I> source, final P target)
	{
		return this.executeSelection(source, target);
	}

	public static <I> CqlTransfer<I, XSequence<I>> New()
	{
		return new Default<>(null, null, null, null, null, null);
	}

	public static <I> CqlTransfer<I, XSequence<I>> New(
		final XIterable<? extends I> source  ,
		final Long                   skip    ,
		final Long                   limit   ,
		final Predicate<? super I>   selector,
		final Comparator<? super I>  order
	)
	{
		return new Default<>(source, skip, limit, selector, order, CqlResultor.New());
	}

	public static <I, T extends Consumer<I> & XIterable<I>> CqlTransfer<I, T> New(
		final XIterable<? extends I> source  ,
		final Long                   skip    ,
		final Long                   limit   ,
		final Predicate<? super I>   selector,
		final Comparator<? super I>  order   ,
		final T                      target
	)
	{
		return new Default<>(source, skip, limit, selector, order, CqlResultor.New(target));
	}

	public static <I, R extends XIterable<I>> CqlTransfer<I, R> New(
		final XIterable<? extends I> source  ,
		final Long                   skip    ,
		final Long                   limit   ,
		final Predicate<? super I>   selector,
		final Comparator<? super I>  order   ,
		final CqlResultor<I, R>         resultor
	)
	{
		return new Default<>(source, skip, limit, selector, order, resultor);
	}

	final class Default<I, R extends XIterable<I>> extends Abstract<I, I, R> implements CqlTransfer<I, R>
	{
		Default(
			final XIterable<? extends I> source  ,
			final Long                   skip    ,
			final Long                   limit   ,
			final Predicate<? super I>   selector,
			final Comparator<? super I>  order   ,
			final CqlResultor<I, R>         resultor
		)
		{
			super(source, skip, limit, selector, Function.identity(), order, resultor);
		}

	}

}
