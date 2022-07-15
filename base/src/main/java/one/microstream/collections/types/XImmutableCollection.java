package one.microstream.collections.types;

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

import one.microstream.collections.BulkList;
import one.microstream.functional.Aggregator;
import one.microstream.typing.Immutable;

public interface XImmutableCollection<E> extends XGettingCollection<E>, Immutable
{
	public interface Factory<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XImmutableCollection<E> newInstance();
	}


	public static <E> Aggregator<E, XImmutableCollection<E>> Builder()
	{
		return Builder(1);
	}

	public static <E> Aggregator<E, XImmutableCollection<E>> Builder(final long initialCapacity)
	{
		return new Aggregator<E, XImmutableCollection<E>>()
		{
			private final BulkList<E> newInstance = BulkList.New(initialCapacity);

			@Override
			public final void accept(final E element)
			{
				this.newInstance.add(element);
			}

			@Override
			public final XImmutableCollection<E> yield()
			{
				return this.newInstance.immure();
			}
		};
	}



	@Override
	public XImmutableCollection<E> copy();

	/**
	 * Always returns the already immutable collection instance itself
	 * <p>
	 * For spawning a copy of the collection instance, see {@link #copy()}
	 *
	 * @return a reference to the instance itself.
	 * @see #copy()
	 */
	@Override
	public XImmutableCollection<E> immure();

}
