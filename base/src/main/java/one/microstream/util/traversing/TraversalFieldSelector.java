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

import java.lang.reflect.Field;
import java.util.function.Predicate;

public interface TraversalFieldSelector
{
	// Field only knows its declaring class, not the actual class, which can be very important for making the decision.
	public boolean test(Class<?> actualClass, Field field);
	
	
	
	public static TraversalFieldSelector New(final Predicate<? super Field> simplePredicate)
	{
		return new TraversalFieldSelector.Default(
			notNull(simplePredicate)
		);
	}
	
	public final class Default implements TraversalFieldSelector
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Predicate<? super Field> simplePredicate;

		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default(final Predicate<? super Field> simplePredicate)
		{
			super();
			this.simplePredicate = simplePredicate;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public boolean test(final Class<?> actualClass, final Field field)
		{
			return this.simplePredicate.test(field);
		}
		
		
	}
}
