package one.microstream.experimental.binaryread.structure;

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

import one.microstream.experimental.binaryread.exception.EntityMemberTypeNotDefined;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

public enum EntityMemberType
{
    REFERENCE, STRING, PRIMITIVE, PRIMITIVE_WRAPPER, PRIMITIVE_COLLECTION, COLLECTION, OPTIONAL, TIMESTAMP_BASED, ARRAY, COMPLEX, ENUM;


    public static EntityMemberType forDefinition(final PersistenceTypeDefinitionMember typeDefinitionMember)
    {
        if (isString(typeDefinitionMember))
        {
            return EntityMemberType.STRING;
        }
        if (isPrimitive(typeDefinitionMember))
        {
            return EntityMemberType.PRIMITIVE;
        }
        if (isPrimitiveWrapper(typeDefinitionMember))
        {
            return EntityMemberType.PRIMITIVE_WRAPPER;
        }
        if (isComplex(typeDefinitionMember))
        {
            return EntityMemberType.COMPLEX;
        }
        if (isArray(typeDefinitionMember))
        {
            return EntityMemberType.ARRAY;
        }
        if (isTimestampBased(typeDefinitionMember))
        {
            return EntityMemberType.TIMESTAMP_BASED;
        }
        if (isEnum(typeDefinitionMember))
        {
            return EntityMemberType.ENUM;
        }
        if (isOptional(typeDefinitionMember))
        {
            return EntityMemberType.OPTIONAL;
        }
        if (isPrimitiveCollection(typeDefinitionMember))
        {
            return EntityMemberType.PRIMITIVE_COLLECTION;
        }
        if (isCollection(typeDefinitionMember))
        {
            return EntityMemberType.COLLECTION;
        }
        if (isReference(typeDefinitionMember))
        {
            // Many other types are also references, but we might inline then in the export.
            return EntityMemberType.REFERENCE;
        }
        throw new EntityMemberTypeNotDefined(typeDefinitionMember);
    }

    private static boolean isPrimitiveCollection(final PersistenceTypeDefinitionMember member)
    {
        return "[C".equals(member.typeName()) ||
                "[B".equals(member.typeName()) ||
                "[Z".equals(member.typeName()) ||
                "[S".equals(member.typeName()) ||
                "[I".equals(member.typeName()) ||
                "[J".equals(member.typeName()) ||
                "[F".equals(member.typeName()) ||
                "[D".equals(member.typeName()) ||
                "[Ljava.lang.Integer;".equals(member.typeName()) ||
                "[Ljava.lang.Long;".equals(member.typeName()) ||
                "[Ljava.lang.Byte;".equals(member.typeName()) ||
                "[Ljava.lang.Short;".equals(member.typeName()) ||
                "[Ljava.lang.Float;".equals(member.typeName()) ||
                "[Ljava.lang.Double;".equals(member.typeName()) ||
                "[Ljava.lang.Character;".equals(member.typeName()) ||
                "[Ljava.lang.Boolean;".equals(member.typeName()) ||
                "[Ljava.lang.String;".equals(member.typeName()) ||
                "[Ljava.math.BigInteger;".equals(member.typeName()) ||
                "[Ljava.math.BigDecimal;".equals(member.typeName());

    }

    private static boolean isCollection(final PersistenceTypeDefinitionMember member)
    {
        // Will be handled as Primitive Collection when we export and see the type is a Primitive Wrapper.
        return "java.util.List".equals(member.typeName()) ||
                "java.util.Set".equals(member.typeName()) ||
                "java.util.Map".equals(member.typeName());
    }

    private static boolean isReference(final PersistenceTypeDefinitionMember member)
    {
        return member.isReference();

    }

    private static boolean isComplex(final PersistenceTypeDefinitionMember member)
    {
        return "[list]".equals(member.typeName());

    }

    private static boolean isArray(final PersistenceTypeDefinitionMember member)
    {
        return "[char]".equals(member.typeName()) || "[byte]".equals(member.typeName());

    }

    private static boolean isString(final PersistenceTypeDefinitionMember member)
    {
        return "java.lang.String".equals(member.typeName());

    }

    private static boolean isPrimitive(final PersistenceTypeDefinitionMember member)
    {
        return member.isFixedLength() && member.isPrimitive();

    }

    private static boolean isEnum(final PersistenceTypeDefinitionMember member)
    {
        return member.typeName()
                .startsWith("enum");

    }

    private static boolean isPrimitiveWrapper(final PersistenceTypeDefinitionMember member)
    {
        return "java.lang.Integer".equals(member.typeName()) ||
                "java.lang.Long".equals(member.typeName()) ||
                "java.lang.Float".equals(member.typeName()) ||
                "java.lang.Double".equals(member.typeName()) ||
                "java.lang.Boolean".equals(member.typeName()) ||
                "java.lang.Character".equals(member.typeName()) ||
                "java.lang.Byte".equals(member.typeName()) ||
                "java.lang.Short".equals(member.typeName()) ||
                "java.math.BigInteger".equals(member.typeName()) ||
                "java.math.BigDecimal".equals(member.typeName());

    }

    private static boolean isTimestampBased(final PersistenceTypeDefinitionMember member)
    {
        return "java.util.Date".equals(member.typeName()) ||
                "java.time.LocalDate".equals(member.typeName()) ||
                "java.time.LocalDateTime".equals(member.typeName()) ||
                "java.time.Instant".equals(member.typeName()) ||
                "java.sql.Timestamp".equals(member.typeName());

    }

    private static boolean isOptional(final PersistenceTypeDefinitionMember member)
    {
        return "java.util.Optional".equals(member.typeName());
    }
}
