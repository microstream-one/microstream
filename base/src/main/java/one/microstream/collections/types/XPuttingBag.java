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


public interface XPuttingBag<E> extends XAddingBag<E>, XPuttingCollection<E>
{
	public interface Creator<E> extends XAddingBag.Factory<E>, XPuttingCollection.Creator<E>
	{
		@Override
		public XPuttingBag<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XAddingCollection<E> addAll(E... elements);

	@Override
	public XAddingCollection<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddingCollection<E> addAll(XGettingCollection<? extends E> elements);


	/**
	 * {@inheritDoc}
	 * <p>
	 * In this implementation it is identical to {@link XPuttingBag#addAll(Object...)}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public XPuttingCollection<E> putAll(E... elements);

	/**
	 * {@inheritDoc}
	 * <p>
	 * In this implementation it is identical to {@link XPuttingBag#addAll(Object[], int, int)}
	 */
	@Override
	public XPuttingCollection<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	/**
	 * {@inheritDoc}
	 * <p>
	 * In this implementation it is identical to {@link XPuttingBag#addAll(XGettingCollection)}
	 */
	@Override
	public XPuttingCollection<E> putAll(XGettingCollection<? extends E> elements);

}
