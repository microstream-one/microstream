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

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGenericComplex;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;

public class ValueReaderArrayOfLists extends ValueReaderVariableLength
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final ValueReader readers[];

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ValueReaderArrayOfLists(final PersistenceTypeDefinitionMember typeDefinition)
	{
		super(typeDefinition);

		final PersistenceTypeDefinitionMemberFieldGenericComplex.Default instance = (PersistenceTypeDefinitionMemberFieldGenericComplex.Default) typeDefinition;
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> instanceMembers = instance.members();

		this.readers = new ValueReader[instanceMembers.intSize()];
		for(int i = 0; i< instanceMembers.intSize(); i++)
		{
			this.readers[i] = ValueReader.deriveValueReader((PersistenceTypeDefinitionMember) instanceMembers.at(i));
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object readValue(final Binary binary, final long offset)
	{

		long listOffset = Binary.toBinaryListElementsOffset(offset);
		final int elementCount = (int) binary.getBinaryListElementCountUnvalidating(offset);

		final Object lists[] = new Object[elementCount];

		for(int j = 0; j < elementCount; j++)
		{
			final Object[] objectValues = new Object[this.readers.length];
			for(int i = 0; i < this.readers.length; i++)
			{
				objectValues[i] = this.readers[i].readValue(binary, listOffset);
				final long size = this.readers[i].getBinarySize(binary, listOffset);
				listOffset += size;
			}

			lists[j] = objectValues;
		}

		return lists;
	}
}
