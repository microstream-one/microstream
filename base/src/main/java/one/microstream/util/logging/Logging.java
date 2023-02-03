package one.microstream.util.logging;

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

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.collections.EqHashTable;
import one.microstream.math.XMath;
import one.microstream.typing.KeyValue;
import one.microstream.util.BundleInfo;

/**
 * Static utility collection for various logging purposes.
 * 
 */
public final class Logging
{
	private final static EqHashTable<Object, Function<Object, String>> toStringConverters = EqHashTable.New();
	
	static
	{
		try
		{
			getLogger(Logging.class).info(
				"MicroStream Version {}"       ,
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
	
	/**
	 * Get the logging facade for a specific class.
	 * 
	 * @param clazz the requesting context
	 * @return the logging facade
	 */
	public static Logger getLogger(final Class<?> clazz)
	{
		return LoggerFactory.getLogger(clazz);
	}
	
	/**
	 * The default Object to String converter, which returns an empty String.
	 * 
	 * @return Object to String converter which returns an empty String
	 */
	private static Function<Object, String> DefaultToStringConverter()
	{
		return obj -> "";
	}
	
	/**
	 * Gets the Object to String converter for a specific context,
	 * or the {@link #DefaultToStringConverter()} if none was specified before.
	 * 
	 * @param context the requesting context
	 * @return an Object to String converter
	 */
	public static Function<Object, String> getToStringConverter(final Object context)
	{
		final Function<Object, String> function = toStringConverters.get(context);
		return function != null
			? function
			: DefaultToStringConverter()
		;
	}
	
	/**
	 * Registers an Object to String converter for a specific context.
	 * 
	 * @param context   the context to register the converter for
	 * @param converter the Object to String converter
	 * @return the old converter which was registered before, or null
	 */
	public static Function<Object, String> setToStringConverter(
		final Object                   context  ,
		final Function<Object, String> converter
	)
	{
		final KeyValue<Object, Function<Object, String>> old = toStringConverters.putGet(
			notNull(context  ),
			notNull(converter)
		);
		return old != null
			? old.value()
			: null
		;
	}
	
	/**
	 * Creates a new lazy argument for a specific context, whose Object to String converter will be used.
	 * The lazy args {@link #toString()} method is only called on demand.
	 * 
	 * @param context  the context to register the lazy arg for
	 * @param argument the argument
	 * @return the lazy object
	 */
	public static final Object LazyArgInContext(
		final Object context ,
		final Object argument
	)
	{
		return LazyArgInContext(context, () -> argument);
	}
	
	/**
	 * Creates a new lazy argument for a specific context, whose Object to String converter will be used.
	 * The lazy args {@link #toString()} method is only called on demand.
	 * 
	 * @param context  the context to register the lazy arg for
	 * @param supplier the argument supplier
	 * @return the lazy object
	 */
	public static final Object LazyArgInContext(
		final Object      context ,
		final Supplier<?> supplier
	)
	{
		notNull(context );
		notNull(supplier);
		
		return new Object()
		{
			@Override
			public String toString()
			{
				return getToStringConverter(context).apply(supplier.get())
				;
			}
		};
	}
	
	/**
	 * Creates a lazy argument.
	 * The lazy args {@link #toString()} method is only called on demand.
	 * 
	 * @param argument the argument
	 * @return the lazy argument
	 */
	public static final Object LazyArg(final Object argument)
	{
		return LazyArg(() -> argument);
	}

	/**
	 * Creates a lazy argument.
	 * The lazy args {@link #toString()} method is only called on demand.
	 * 
	 * @param supplier the argument supplier
	 * @return the lazy object
	 */
	public static final Object LazyArg(final Supplier<?> supplier)
	{
		notNull(supplier);
		
		return new Object()
		{
			@Override
			public String toString()
			{
				return String.valueOf(supplier.get());
			}
		};
	}

	/**
	 * Creates an Object to String converter, which only includes a limited amount of elements
	 * in the resulting String, if {@link Iterable}s or arrays are to be converted.
	 * <p>
	 * For all other object's {@link String#valueOf(Object)} is used as converter.
	 * <p>
	 * For example, a {@link List} of {@link Integer}s from 1 to 100, and a limit of 3,
	 * will result in a {@link String} like this:<br>
	 * <code>"[1, 2, 3, ...]"</code>
	 * 
	 * @param limit maximum amount of converted elements in {@link Iterable}s and arrays
	 * @return the new Object to String converter
	 */
	public static Function<Object, String> LimitedElementsToStringConverter(final int limit)
	{
		return LimitedElementsToStringConverter(
			limit          ,
			String::valueOf
		);
	}
		
	/**
	 * Creates an Object to String converter, which only includes a limited amount of elements
	 * in the resulting String, if {@link Iterable}s or arrays are to be converted.
	 * <p>
	 * For all other object's the supplied <code>toStringConverter</code> will be used.
	 * <p>
	 * For example, a {@link List} of {@link Integer}s from 1 to 100, and a limit of 3,
	 * will result in a {@link String} like this:<br>
	 * <code>"[1, 2, 3, ...]"</code>
	 * 
	 * @param limit maximum amount of converted elements in {@link Iterable}s and arrays
	 * @param toStringConverter the converter for all other objects
	 * @return the new Object to String converter
	 */
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
	
	/**
	 * Creates an Object to String converter, which only includes a limited amount of elements
	 * in the resulting String, if {@link Iterable}s or arrays are to be converted.
	 * <p>
	 * For all other object's the supplied <code>toStringConverter</code> will be used.
	 * <p>
	 * For example, a {@link List} of {@link Integer}s from 1 to 100, and a limit of 3,
	 * will result in a {@link String} like this:<br>
	 * <code>"[1, 2, 3, ...]"</code>
	 * 
	 * @param limitProvider provides maximum amount of converted elements in {@link Iterable}s and arrays
	 * @param toStringConverter the converter for all other objects
	 * @return the new Object to String converter
	 */
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
	
	
	private static class LimitedElementsToStringConverter implements Function<Object, String>
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
