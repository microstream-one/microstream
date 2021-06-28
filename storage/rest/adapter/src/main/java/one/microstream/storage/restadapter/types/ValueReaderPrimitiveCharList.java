package one.microstream.storage.restadapter.types;

/*-
 * #%L
 * microstream-storage-restadapter
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

public class ValueReaderPrimitiveCharList extends ValueReaderVariableLength
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ValueReaderPrimitiveCharList(final PersistenceTypeDefinitionMember typeDefinition)
	{
		super(typeDefinition);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object readValue(final Binary binary, final long offset)
	{
		long listOffset = Binary.toBinaryListElementsOffset(offset);
		final int elementCount = (int) binary.getBinaryListElementCountUnvalidating(offset);

		final Object[] values = new Object[elementCount];
		for(int i = 0; i < elementCount; i++)
		{
			values[i] = binary.read_char(listOffset);
			listOffset += BinaryPersistence.resolveFieldBinaryLength(char.class);
		}

		return values;

	}
}
