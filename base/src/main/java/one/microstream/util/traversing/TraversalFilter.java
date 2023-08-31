package one.microstream.util.traversing;

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

import java.util.function.Predicate;

import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XEnum;
import one.microstream.collections.types.XSet;

public interface TraversalFilter<P extends TraversalPredicate>
{
	public ObjectGraphTraverserBuilder builder();
	
	public XSet<Object> instances();
	
	public XSet<Class<?>> types();
	
	public XEnum<Class<?>> typesPolymorphic();
	
	public P predicate();
	
	public Predicate<Object> customPredicate();
	
	public boolean isEmpty();
	
	
	
	public ObjectGraphTraverserBuilder setPredicate(final P predicate);

	public default ObjectGraphTraverserBuilder instance(final Object instance)
	{
		synchronized(this.builder())
		{
			this.instances().add(instance);
		}
		return this.builder();
	}

	public default ObjectGraphTraverserBuilder instances(final Object... instances)
	{
		synchronized(this.builder())
		{
			this.instances().addAll(instances);
		}
		return this.builder();
	}
	
	public default ObjectGraphTraverserBuilder instances(final Iterable<?> instances)
	{
		synchronized(this.builder())
		{
			final XSet<Object> is = this.instances();
			for(final Object instance : instances)
			{
				is.add(instance);
			}
		}
		return this.builder();
	}
		
	public ObjectGraphTraverserBuilder custom(final Predicate<Object> customPredicate);
	
	public default ObjectGraphTraverserBuilder type(final Class<?> type)
	{
		synchronized(this.builder())
		{
			this.types().add(type);
		}
		return this.builder();
	}

	public default ObjectGraphTraverserBuilder types(final Class<?>... types)
	{
		synchronized(this.builder())
		{
			this.types().addAll(types);
		}
		return this.builder();
	}
	
	public default ObjectGraphTraverserBuilder types(final Iterable<Class<?>> types)
	{
		synchronized(this.builder())
		{
			final XSet<Class<?>> ts = this.types();
			for(final Class<?> type : types)
			{
				ts.add(type);
			}
		}
		return this.builder();
	}
	
	public default ObjectGraphTraverserBuilder typePolymorphic(final Class<?> type)
	{
		synchronized(this.builder())
		{
			this.typesPolymorphic().add(type);
		}
		return this.builder();
	}

	public default ObjectGraphTraverserBuilder typesPolymorphic(final Class<?>... types)
	{
		synchronized(this.builder())
		{
			this.typesPolymorphic().addAll(types);
		}
		return this.builder();
	}
	
	public default ObjectGraphTraverserBuilder typesPolymorphic(final Iterable<Class<?>> types)
	{
		synchronized(this.builder())
		{
			final XEnum<Class<?>> tps = this.typesPolymorphic();
			for(final Class<?> type : types)
			{
				tps.add(type);
			}
		}
		
		return this.builder();
	}
	
	
	
	public static <P extends TraversalPredicate> TraversalFilter.Default<P> New(
		final ObjectGraphTraverserBuilder builder
	)
	{
		return new TraversalFilter.Default<>(
			notNull(builder)
		);
	}
	
	public final class Default<P extends TraversalPredicate> implements TraversalFilter<P>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ObjectGraphTraverserBuilder builder         ;
		private final XSet<Object>                instances       ;
		private final XSet<Class<?>>              types           ;
		private final XEnum<Class<?>>             typesPolymorphic;
		private       P                           predicate       ;
		private       Predicate<Object>           customPredicate ;
		
		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(final ObjectGraphTraverserBuilder builder)
		{
			super();
			this.builder          = builder       ;
			this.instances        = HashEnum.New();
			this.types            = HashEnum.New();
			this.typesPolymorphic = HashEnum.New();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public boolean isEmpty()
		{
			synchronized(this.builder())
			{
				return this.instances.isEmpty() && this.types.isEmpty() && this.typesPolymorphic.isEmpty()
					&& this.customPredicate == null
				;
			}
		}

		@Override
		public ObjectGraphTraverserBuilder builder()
		{
			return this.builder;
		}

		@Override
		public XSet<Object> instances()
		{
			synchronized(this.builder())
			{
				return this.instances;
			}
		}

		@Override
		public XSet<Class<?>> types()
		{
			synchronized(this.builder())
			{
				return this.types;
			}
		}

		@Override
		public XEnum<Class<?>> typesPolymorphic()
		{
			synchronized(this.builder())
			{
				return this.typesPolymorphic;
			}
		}

		@Override
		public P predicate()
		{
			synchronized(this.builder())
			{
				return this.predicate;
			}
		}

		@Override
		public Predicate<Object> customPredicate()
		{
			synchronized(this.builder())
			{
				return this.customPredicate;
			}
		}

		@Override
		public ObjectGraphTraverserBuilder setPredicate(final P predicate)
		{
			synchronized(this.builder())
			{
				this.predicate = predicate;
			}
			return this.builder();
		}

		@Override
		public ObjectGraphTraverserBuilder custom(final Predicate<Object> customPredicate)
		{
			synchronized(this.builder())
			{
				this.customPredicate = customPredicate;
			}
			return this.builder();
		}
		
	}
	
}
