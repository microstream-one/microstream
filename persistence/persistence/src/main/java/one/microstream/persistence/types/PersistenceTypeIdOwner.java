package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

import one.microstream.collections.XUtilsCollection;
import one.microstream.collections.types.XSortableSequence;

public interface PersistenceTypeIdOwner
{
	public long typeId();



	public static int orderAscending(final PersistenceTypeIdOwner o1, final PersistenceTypeIdOwner o2)
	{
		return o2.typeId() >= o1.typeId() ? o2.typeId() > o1.typeId() ? -1 : 0 : +1;
	}


	public static <E extends PersistenceTypeIdOwner, C extends XSortableSequence<E>>
	C sortByTypeIdAscending(final C elements)
	{
		return XUtilsCollection.valueSort(elements, PersistenceTypeIdOwner::orderAscending);
	}

}
