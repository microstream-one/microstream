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

import java.util.function.Predicate;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingSet;

@FunctionalInterface
public interface TraversalPredicateSkip extends TraversalPredicate
{
	public boolean skip(Object instance);
	
	
	
	public static TraversalPredicateSkip New(
		final Predicate<Object>          customPredicate ,
		final XGettingSet<Class<?>>      positiveTypes   ,
		final XGettingSequence<Class<?>> typesPolymorphic
	)
	{
		return new TraversalPredicateSkip.Default(
			customPredicate ,
			positiveTypes   ,
			typesPolymorphic
		);
	}
	
	public final class Default extends AbstractHandlingPredicate implements TraversalPredicateSkip
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final Predicate<Object>          customPredicate  ,
			final XGettingSet<Class<?>>      positiveTypes    ,
			final XGettingSequence<Class<?>> typesPolymorphic
		)
		{
			super(customPredicate, positiveTypes, typesPolymorphic);
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final boolean skip(final Object instance)
		{
			return this.test(instance);
		}
		
	}
	
}
