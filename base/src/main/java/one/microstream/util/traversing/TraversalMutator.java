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

import java.util.function.Function;
import java.util.function.Predicate;

public interface TraversalMutator extends TraversalHandler
{
	public Object mutateReference(Object instance, Object parent);
	
		
	public static TraversalMutator New(final Function<Object, ?> logic)
	{
		return new TraversalMutator.Default(logic);
	}
		
	public static TraversalMutator New(final Predicate<Object> condition, final Function<Object, ?> logic)
	{
		return new TraversalMutator.Conditional(condition, logic);
	}
		
	public final class Default implements TraversalMutator
	{
		private final Function<Object, ?> logic;

		Default(final Function<Object, ?> logic)
		{
			super();
			this.logic = logic;
		}

		@Override
		public final Object mutateReference(final Object instance, final Object parent)
		{
			return this.logic.apply(instance);
		}
		
	}

	public final class Conditional implements TraversalMutator
	{
		private final Predicate<Object>   condition;
		private final Function<Object, ?> logic    ;

		Conditional(final Predicate<Object> condition, final Function<Object, ?> logic)
		{
			super();
			this.condition = condition;
			this.logic     = logic    ;
		}

		@Override
		public final Object mutateReference(final Object instance, final Object parent)
		{
			if(this.condition.test(instance))
			{
				return this.logic.apply(instance);
			}
			return instance;
		}
		
	}
	
}
