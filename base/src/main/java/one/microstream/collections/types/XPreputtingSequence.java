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

public interface XPreputtingSequence<E> extends XPrependingSequence<E>
{
	public interface Creator<E> extends XPrependingSequence.Creator<E>
	{
		@Override
		public XPreputtingSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPreputtingSequence<E> prependAll(E... elements);

	@Override
	public XPreputtingSequence<E> prependAll(E[] elements, int offset, int length);

	@Override
	public XPreputtingSequence<E> prependAll(XGettingCollection<? extends E> elements);

	public boolean preput(E element);

	public boolean nullPreput();

	@SuppressWarnings("unchecked")
	public XPreputtingSequence<E> preputAll(E... elements);

	public XPreputtingSequence<E> preputAll(E[] elements, int offset, int length);

	public XPreputtingSequence<E> preputAll(XGettingCollection<? extends E> elements);

}
