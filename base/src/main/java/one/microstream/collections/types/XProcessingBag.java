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



public interface XProcessingBag<E> extends XRemovingBag<E>, XGettingBag<E>, XProcessingCollection<E>
{
	public interface Factory<E>
	extends XRemovingBag.Factory<E>, XGettingBag.Factory<E>, XProcessingCollection.Factory<E>
	{
		@Override
		public XProcessingBag<E> newInstance();
	}



	@Override
	public XProcessingBag<E> copy();

	@Override
	public XGettingBag<E> view();


	/**
	 * Provides an instance of an immutable collection type with equal behavior and data as this instance.
	 * <p>
	 * If this instance already is of an immutable collection type, it returns itself.
	 *
	 * @return an immutable copy of this collection instance.
	 */
	@Override
	public XImmutableBag<E> immure();

}
