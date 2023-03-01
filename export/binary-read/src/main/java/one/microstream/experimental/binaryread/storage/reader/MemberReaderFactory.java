package one.microstream.experimental.binaryread.storage.reader;

/*-
 * #%L
 * binary-read
 * %%
 * Copyright (C) 2023 MicroStream Software
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

import one.microstream.experimental.binaryread.ReadingContext;
import one.microstream.experimental.binaryread.exception.ReaderNotDefinedYetException;
import one.microstream.experimental.binaryread.structure.EntityMember;
import one.microstream.experimental.binaryread.structure.EntityMemberType;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

public class MemberReaderFactory
{

    public static MemberReader define(final ReadingContext readingContext, final EntityMemberType entityMemberType, final EntityMember entityMember)
    {
        final PersistenceTypeDefinitionMember typeDefinition = entityMember.getTypeDefinitionMember();
        final MemberReader result;
        switch (entityMemberType)
        {

            case REFERENCE:
            case STRING:
            case PRIMITIVE_WRAPPER:
            case PRIMITIVE_COLLECTION:
            case COLLECTION:
            case TIMESTAMP_BASED:
            case OPTIONAL:
            case ENUM_ARRAY:
                // There are many different types to make easier usage of the data.
                result = new ReferenceReader(readingContext, entityMember);
                break;
            case PRIMITIVE:
                result = new PrimitiveReader(readingContext, entityMember);
                break;
            case ARRAY:
                result = new ArrayReader(readingContext, entityMember);
                break;
            case COMPLEX:
                result = new SpecialListReader(readingContext, entityMember);
                break;
            case ENUM:
                result = new EnumReader(readingContext, entityMember);
                break;
            default:
                throw new ReaderNotDefinedYetException(typeDefinition);
        }
        return result;
    }
}
