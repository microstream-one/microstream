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

import java.util.function.Consumer;


/**
 * @param <E> type of contained elements
 * 
 *
 */
public interface XGettingSet<E> extends XGettingCollection<E>
{
	public interface Creator<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XGettingSet<E> newInstance();
	}



	@Override
	public XImmutableSet<E> immure();

	@Override
	public XGettingSet<E> copy();

	@Override
	public <P extends Consumer<? super E>> P iterate(P procedure);

}
