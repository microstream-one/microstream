package one.microstream.util.iterables;

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

import java.util.Iterator;

public class SynchronizedIterator<E> implements Iterator<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Iterator<E> iterator;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SynchronizedIterator(final Iterator<E> iterator)
	{
		super();
		this.iterator = iterator;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public synchronized boolean hasNext()
	{
		return this.iterator.hasNext();
	}

	@Override
	public synchronized void remove()
	{
		this.iterator.remove();
	}

	@Override
	public synchronized E next()
	{
		return this.iterator.next();
	}

}
