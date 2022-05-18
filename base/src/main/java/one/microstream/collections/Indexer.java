package one.microstream.collections;

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

import one.microstream.X;
import one.microstream.functional.IndexedAcceptor;

final class Indexer<E> implements IndexedAcceptor<E>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private static final int INITIAL_INDEX_LENGTH = 32;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private int                        size     ;
	private int[]                      index    ;
	private final Predicate<? super E> predicate;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	Indexer(final Predicate<? super E> predicate)
	{
		super();
		this.size = 0;
		this.index = new int[INITIAL_INDEX_LENGTH];
		this.predicate = predicate;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E e, final long index)
	{
		if(!this.predicate.test(e))
		{
			return;
		}

		if(this.size >= this.index.length)
		{
			System.arraycopy(this.index, 0, this.index = new int[(int)(this.index.length * 2.0f)], 0, this.size);
		}
		this.index[this.size++] = X.checkArrayRange(index);
	}

	public final int[] yield()
	{
		if(this.size >= this.index.length)
		{
			return this.index;
		}

		final int[] index;
		System.arraycopy(this.index, 0, index = new int[this.size], 0, this.size);
		return index;
	}

	public final int[] sortAndYield()
	{
		XSort.sort(this.index, 0, this.size);
		return this.yield();
	}

}
