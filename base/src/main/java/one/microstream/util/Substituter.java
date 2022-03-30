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

import one.microstream.chars.VarString;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XIterable;
import one.microstream.hashing.HashEqualator;
import one.microstream.typing.Composition;

public interface Substituter<T>
{
	public T substitute(T s);



	public interface Removing<T> extends Substituter<T>, one.microstream.typing.Clearable
	{
		@Override
		public void clear();

		public T remove(T instance);
	}

	public interface Iterable<T> extends Substituter<T>, XIterable<T>
	{
		// empty so far
	}

	public interface Queryable<T> extends Substituter<T>
	{
		public boolean contains(T instance);
		
		public T lookup(T instance);
		
		public long size();
	}

	public interface Managed<T> extends Removing<T>, Iterable<T>, Queryable<T>
	{
		// only type combining type
	}


	public static <T> Substituter.Default<T> New()
	{
		return new Default<>(EqHashEnum.<T>New());
	}

	public static <T> Substituter.Default<T> New(final HashEqualator<? super T> hashEqualator)
	{
		return new Default<>(EqHashEnum.New(hashEqualator));
	}

	public static <T> Substituter.Default<T> New(final EqHashEnum<T> elements)
	{
		return new Default<>(notNull(elements));
	}



	public final class Default<T> implements Substituter.Managed<T>, Composition
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private static final int MAX_TO_STRING_ITEMS = 10;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final EqHashEnum<T> elements;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final EqHashEnum<T> elements)
		{
			super();
			this.elements = elements;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public final XGettingEnum<T> elements()
		{
			return this.elements;
		}

		@Override
		public final synchronized T substitute(final T item)
		{
			if(item == null)
			{
				return null;
			}
			return this.elements.deduplicate(item);
		}

		@Override
		public final synchronized void clear()
		{
			this.elements.clear();
		}

		@Override
		public final synchronized <P extends Consumer<? super T>> P iterate(final P procedure)
		{
			this.elements.iterate(procedure);
			return procedure;
		}

		@Override
		public final synchronized boolean contains(final T item)
		{
			return this.elements.contains(item);
		}
		
		@Override
		public final synchronized T lookup(final T instance)
		{
			return this.elements.seek(instance);
		}

		@Override
		public final synchronized T remove(final T item)
		{
			return this.elements.retrieve(item);
		}
		
		@Override
		public final synchronized long size()
		{
			return this.elements.size();
		}

		@Override
		public final String toString()
		{
			return this.iterate(
				new Consumer<T>()
				{
					final VarString vs    = VarString.New(1000);
					      int       count;

					@Override
					public void accept(final T e)
					{
						if(++this.count > MAX_TO_STRING_ITEMS)
						{
							return;
						}
						this.vs.add(e).add(',');
					}

					final String yield()
					{
						if(this.count > MAX_TO_STRING_ITEMS)
						{
							this.vs.add("... [" + (this.count - MAX_TO_STRING_ITEMS) + " more]");
						}
						else if(!this.vs.isEmpty())
						{
							this.vs.deleteLast(); // delete trailing comma.
						}
						return this.vs.toString();
					}
				}
			).yield();
		}
		
		///////////////////////////////////////////////////////////////////////////
		// Hooks for TypeHandler //
		//////////////////////////
		
		public EqHashEnum<T> $elements()
		{
			return this.elements;
		}

	}

}
