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

public interface XRemovingSequence<E> extends XRemovingCollection<E>
{
	public XRemovingSequence<E> removeRange(long offset, long length);

	/**
	 * Removing all elements but the ones from the offset (basically start index)
	 * to the offset+length (end index).
	 * 
	 * @param offset is the index of the first element to retain
	 * @param length is the amount of elements to retain
	 * @return this
	 */
	public XRemovingSequence<E> retainRange(long offset, long length);

	public long removeSelection(long[] indices);


	public interface Factory<E> extends XRemovingCollection.Factory<E>
	{
		@Override
		public XRemovingSequence<E> newInstance();
	}

}
