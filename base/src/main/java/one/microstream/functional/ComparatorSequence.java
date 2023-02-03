package one.microstream.functional;

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

import java.util.Comparator;

/**
 * Helper class to chain multiple {@link Comparator} functions together as a super {@link Comparator}.<br>
 * Useful for implementing SQL-like "ORDER BY" for querying / processing collections.
 *
 */
public class ComparatorSequence<T> implements Comparator<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Comparator<? super T>[] comparators;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	@SafeVarargs
	public ComparatorSequence(final Comparator<? super T>... comparators)
	{
		super();
		this.comparators = comparators;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public int compare(final T o1, final T o2)
	{
		// fields not cached as local variables as array is not expected to be long enough to pay off. Or VM does it.
		for(int c, i = 0; i < this.comparators.length; i++)
		{
			// spare foreach's unnecessary local variable
			if((c = this.comparators[i].compare(o1, o2)) != 0)
			{
				return c;
			}
		}
		return 0;
	}

}
