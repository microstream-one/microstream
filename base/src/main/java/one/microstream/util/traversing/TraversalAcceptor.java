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

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface TraversalAcceptor extends TraversalHandler
{
	
	public boolean acceptReference(Object instance, Object parent);
	
	
	public static TraversalAcceptor New(final Consumer<Object> logic)
	{
		return new TraversalAcceptor.Default(logic);
	}
	
	public static TraversalAcceptor New(final Predicate<Object> condition, final Consumer<Object> logic)
	{
		return new TraversalAcceptor.Conditional(condition, logic);
	}
		
	public final class Default implements TraversalAcceptor
	{
		private final Consumer<Object> logic;

		Default(final Consumer<Object> logic)
		{
			super();
			this.logic = logic;
		}

		@Override
		public final boolean acceptReference(final Object instance, final Object parent)
		{
			this.logic.accept(instance);
			return true;
		}
		
	}
	
	public final class Conditional implements TraversalAcceptor
	{
		private final Predicate<Object> condition;
		private final Consumer<Object>  logic    ;

		Conditional(final Predicate<Object> condition, final Consumer<Object> logic)
		{
			super();
			this.condition = condition;
			this.logic     = logic    ;
		}

		@Override
		public final boolean acceptReference(final Object instance, final Object parent)
		{
			if(this.condition.test(instance))
			{
				this.logic.accept(instance);
			}
			return true;
		}
		
	}
		
}
