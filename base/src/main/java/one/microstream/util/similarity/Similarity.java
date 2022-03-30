package one.microstream.util.similarity;

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

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;

public interface Similarity<E>
{
	public E sourceElement();
	
	public double similarity();
	
	public E targetElement();
	
	
	
	public static <E> E sourceElement(final Similarity<E> instance)
	{
		return instance != null
			? instance.sourceElement()
			: null
		;
	}
	
	public static Double similarity(final Similarity<?> instance)
	{
		return instance != null
			? instance.similarity()
			: null
		;
	}
	
	public static double _similarity(final Similarity<?> instance)
	{
		return instance != null
			? instance.similarity()
			: Double.NaN
		;
	}
	
	public static <E> E targetElement(final Similarity<E> instance)
	{
		return instance != null
			? instance.targetElement()
			: null
		;
	}
	
	public static <E> Similarity<E> New(
		final E      sourceElement,
		final double similarity   ,
		final E      targetElement
	)
	{
		/*
		 * No restrictions on the arguments. In the general case,
		 * anything can be compared to anything with any arbitrary similarity.
		 */
		return new Similarity.Default<>(
			sourceElement,
			similarity   ,
			targetElement
		);
	}
	
	@SuppressWarnings("unchecked")
	public static <E> Similarity<E>[] Array(final int length)
	{
		return new Similarity[length];
	}
	
	public class Default<E> implements Similarity<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final E      sourceElement;
		private final double similarity   ;
		private final E      targetElement;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(
			final E      sourceElement,
			final double similarity   ,
			final E      targetElement
		)
		{
			super();
			this.sourceElement = sourceElement;
			this.similarity    = similarity   ;
			this.targetElement = targetElement;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final E sourceElement()
		{
			return this.sourceElement;
		}
		
		@Override
		public final double similarity()
		{
			return this.similarity;
		}
		
		@Override
		public final E targetElement()
		{
			return this.targetElement;
		}
		
		@Override
		public String toString()
		{
			final VarString vs = VarString.New()
				.add(this.sourceElement, XChars::assembleCautiously)
				.add('<', '-')
				.add(MultiMatchAssembler.Defaults.defaultSimilarityFormatter().format(this.similarity))
				.add('-', '>')
				.add(this.targetElement, XChars::assembleCautiously)
			;
			
			return vs.toString();
		}
		
	}
	
}
