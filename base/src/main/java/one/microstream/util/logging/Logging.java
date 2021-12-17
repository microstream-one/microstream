package one.microstream.util.logging;

import static one.microstream.X.notNull;

import java.util.Iterator;

/*-
 * #%L
 * microstream-persistence-binary
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.collections.EqHashTable;
import one.microstream.math.XMath;
import one.microstream.util.BundleInfo;

public final class Logging
{
	private final static EqHashTable<Object, Function<Object, String>> toStringConverters = EqHashTable.New();
	
	static
	{
		try
		{
			getLogger(Logging.class).info(
				"MicroStream Version {}",
				BundleInfo.LoadBase().version()
			);
		}
		catch(final Exception e)
		{
			/*
			 * Swallow and continue.
			 * BundleInfo may not be discovered when the Maven build was not run properly.
			 */
		}
	}
	
	public static Logger getLogger(final Class<?> clazz)
	{
		return LoggerFactory.getLogger(clazz);
	}
	
	private static Function<Object, String> DefaultToStringConverter()
	{
		return obj -> "";
	}
	
	public static Function<Object, String> getToStringConverter(final Object context)
	{
		final Function<Object, String> function = toStringConverters.get(context);
		return function != null
			? function
			: DefaultToStringConverter()
		;
	}
	
	public static Function<Object, String> setToStringConverter(
		final Object                   context  ,
		final Function<Object, String> converter
	)
	{
		toStringConverters.put(context, converter);
		return converter;
	}
	
	public static final Object LazyArgInContext(
		final Object context,
		final Object object
	)
	{
		return LazyArgInContext(context, () -> object);
	}
	
	public static final Object LazyArgInContext(
		final Object      context ,
		final Supplier<?> supplier
	)
	{
		return new Object()
		{
			@Override
			public String toString()
			{
				return getToStringConverter(context).apply(
					supplier.get()
				);
			}
		};
	}

	public static final Object LazyArg(final Supplier<?> supplier)
	{
		return new Object()
		{
			@Override
			public String toString()
			{
				return String.valueOf(supplier.get());
			}
		};
	}

	public static Function<Object, String> LimitedElementsToStringConverter(final int limit)
	{
		return LimitedElementsToStringConverter(limit, String::valueOf);
	}
		
	public static Function<Object, String> LimitedElementsToStringConverter(
		final int                      limit            ,
		final Function<Object, String> toStringConverter
	)
	{
		return LimitedElementsToStringConverter(
			obj -> limit     ,
			toStringConverter
		);
	}
	
	public static Function<Object, String> LimitedElementsToStringConverter(
		final ToIntFunction<Object>    limitProvider    ,
		final Function<Object, String> toStringConverter
	)
	{
		return new Logging.LimitedElementsToStringConverter(
			notNull(limitProvider    ),
			notNull(toStringConverter)
		);
	}
	
	
	static class LimitedElementsToStringConverter implements Function<Object, String>
	{
		private final ToIntFunction<Object>    limitProvider    ;
		private final Function<Object, String> toStringConverter;

		LimitedElementsToStringConverter(
			final ToIntFunction<Object>    limitProvider    ,
			final Function<Object, String> toStringConverter
		)
		{
			super();
			this.limitProvider     = limitProvider    ;
			this.toStringConverter = toStringConverter;
		}

		@Override
		public String apply(final Object obj)
		{
			if(obj instanceof Iterable<?>)
			{
				final int limit = this.limitProvider.applyAsInt(obj);
				return this.toString((Iterable<?>)obj, limit);
			}
			else if(obj != null && obj.getClass().isArray())
			{
				final int limit = this.limitProvider.applyAsInt(obj);
				return this.toString(X.Iterable(obj), limit);
			}
			
			return this.toStringConverter.apply(obj);
		}
		
		private String toString(final Iterable<?> iterable, final int limit)
		{
			XMath.positive(limit);
			
			final VarString   vs       = VarString.New().add('[');
			final Iterator<?> iterator = iterable.iterator();
			      int         position = 0;
			while(position < limit && iterator.hasNext())
			{
				if(position > 0)
				{
					vs.add(", ");
				}
				vs.add(this.apply(iterator.next()));
				position++;
			}
			if(iterator.hasNext())
			{
				vs.add(", ...");
			}
			return vs.add(']').toString();
		}
		
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	* Dummy constructor to prevent instantiation of this static-only utility class.
	* 
	* @throws UnsupportedOperationException when called
	*/
	private Logging()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
