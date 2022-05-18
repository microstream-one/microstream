package one.microstream.util;

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
import java.util.function.Function;
import java.util.function.Supplier;


public interface Executor<EX extends Throwable>
{
	public Executor<EX> reset();

	public boolean handle(Throwable t);

	public void complete(Runnable onSuccessLogics);



	public default void execute(final Runnable logic)
	{
		try
		{
			logic.run();
		}
		catch(final Throwable e)
		{
			if(!this.handle(e))
			{
				throw e;
			}
		}
	}

	public default <E> void execute(final E element, final Consumer<? super E> logic)
	{
		try
		{
			logic.accept(element);
		}
		catch(final Throwable e)
		{
			if(!this.handle(e))
			{
				throw e;
			}
		}
	}

	public default <E> void executeNullIgnoring(final E element, final Consumer<? super E> logic)
	{
		if(element == null)
		{
			return;
		}
		try
		{
			logic.accept(element);
		}
		catch(final Throwable e)
		{
			if(!this.handle(e))
			{
				throw e;
			}
		}
	}

	public default <E> void executeNullHandling(
		final E                   element      ,
		final Consumer<? super E> logic        ,
		final Runnable            nullCaseLogic
	)
	{
		try
		{
			if(element != null)
			{
				logic.accept(element);
			}
			else
			{
				nullCaseLogic.run();
			}
		}
		catch(final Throwable e)
		{
			if(!this.handle(e))
			{
				throw e;
			}
		}
	}

	public default <R> R executeR(final Supplier<? extends R> logic)
	{
		try
		{
			return logic.get();
		}
		catch(final Throwable e)
		{
			if(this.handle(e))
			{
				return null;
			}
			throw e;
		}
	}

	public default <E, R> R executeR(final E element, final Function<? super E, R> logic)
	{
		try
		{
			return logic.apply(element);
		}
		catch(final Throwable e)
		{
			if(this.handle(e))
			{
				return null;
			}
			throw e;
		}
	}

	public default <E, R> R executeRNullIgnoring(final E element, final Function<? super E, R> logic)
	{
		if(element == null)
		{
			return null;
		}
		try
		{
			return logic.apply(element);
		}
		catch(final Throwable e)
		{
			if(this.handle(e))
			{
				return null;
			}
			throw e;
		}
	}

	public default <E, R> R executeRNullHandling(
		final E                      element,
		final Function<? super E, R> logic  ,
		final Supplier<? extends R>  nullCaseLogic
	)
	{
		try
		{
			if(element == null)
			{
				return nullCaseLogic.get();
			}
			return logic.apply(element);
		}
		catch(final Throwable e)
		{
			if(this.handle(e))
			{
				return null;
			}
			throw e;
		}
	}

	public static Executor<Exception> New(final BufferingCollector<? super Exception> collector)
	{
		return New(Exception.class, collector);
	}

	public static Executor<Exception> New(final Consumer<? super Exception> exceptionFinalizer)
	{
		return New(Exception.class, BufferingCollector.New(notNull(exceptionFinalizer)));
	}

	public static <EX extends Throwable> Executor<EX> New(
		final Class<EX>                      exceptionType,
		final BufferingCollector<? super EX> collector
	)
	{
		return new Executor.Default<>(notNull(exceptionType), notNull(collector));
	}

	public static <EX extends Throwable> Executor<EX> New(
		final Class<EX>            exceptionType     ,
		final Consumer<? super EX> exceptionFinalizer
	)
	{
		return new Executor.Default<>(
			notNull(exceptionType),
			BufferingCollector.New(notNull(exceptionFinalizer))
		);
	}

	public final class Default<EX extends Throwable> implements Executor<EX>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final Class<EX>                      type     ;
		final BufferingCollector<? super EX> collector;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final Class<EX> type, final BufferingCollector<? super EX> collector)
		{
			super();
			this.type      = type     ;
			this.collector = collector;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public Executor.Default<EX> reset()
		{
			this.collector.resetElements();
			return this;
		}

		@Override
		public boolean handle(final Throwable t)
		{
			if(this.type.isAssignableFrom(t.getClass()))
			{
				this.collector.accept(this.type.cast(t));
				return true;
			}
			return false;
		}

		@Override
		public void complete(final Runnable onSuccessLogics)
		{
			if(this.collector.isEmpty())
			{
				onSuccessLogics.run();
			}
			else
			{
				this.collector.finalizeElements();
			}
		}

	}

}
