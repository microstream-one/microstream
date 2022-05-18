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

/**
 *
 * 
 *
 * @param <T> the checked element's type
 */
public class SimilatorSequence<T> implements Similator<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Similator<? super T>[] similators;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	@SafeVarargs
	public SimilatorSequence(final Similator<? super T>... comparators)
	{
		super();
		this.similators = comparators;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public double evaluate(final T o1, final T o2)
	{
		double result = 0.0;
		// fields not cached as local variables as array is not expected to be long enough to pay off. Or VM does it.
		for(int i = 0; i < this.similators.length; i++)
		{
			// spare foreach's unnecessary local variable
			result += this.similators[i].evaluate(o1, o2);
		}
		return result / this.similators.length;
	}

}
