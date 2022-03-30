package one.microstream.collections.old;

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

import java.util.Collection;

import one.microstream.collections.BulkList;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XGettingCollection;

public interface OldCollection<E> extends Collection<E>
{
	public XGettingCollection<E> parent();

	@Override
	public default <T> T[] toArray(final T[] target)
	{
		XArrays.copyTo(this.parent(), target);
		return target;
	}


	public default void bla()
	{
		final BulkList<String> ps = null;

		final Number[] ns = new Number[5];

		XArrays.copyTo(ps, ns);
	}


}
