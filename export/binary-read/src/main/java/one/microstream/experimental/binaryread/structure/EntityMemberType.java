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

import one.microstream.experimental.binaryread.ReadingContext;
import one.microstream.experimental.binaryread.exception.EntityMemberTypeNotDefined;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum EntityMemberType
{
    REFERENCE, STRING, PRIMITIVE, PRIMITIVE_WRAPPER, PRIMITIVE_COLLECTION, COLLECTION, DICTIONARY, OPTIONAL, TIMESTAMP_BASED, ARRAY, ENUM_ARRAY, COMPLEX, ENUM;


    public static EntityMemberType forDefinition(final ReadingContext readingContext, final PersistenceTypeDefinitionMember typeDefinitionMember)
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
        if (isEnumArray(typeDefinitionMember, readingContext.getStorage()))
        {
            return EntityMemberType.ENUM_ARRAY;
        }
        if (isTimestampBased(typeDefinitionMember))
        {
            return EntityMemberType.TIMESTAMP_BASED;
        }
        if (isEnum(typeDefinitionMember, readingContext.getStorage()))
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
        if (isDictionary(typeDefinitionMember))
        {
            return EntityMemberType.DICTIONARY;
        }
        if (isReference(typeDefinitionMember))
        {
            // Many other types are also references, but we might inline then in the export.
            return EntityMemberType.REFERENCE;
        }
        throw new EntityMemberTypeNotDefined(typeDefinitionMember);
    }


    private static final Set<String> PRIMITIVE_COLLECTIONS = new HashSet<>(Arrays.asList(
            "[C",
            "[B",
            "[Z",
            "[S",
            "[I",
            "[J",
            "[F",
            "[D",
            "[Ljava.lang.Integer;",
            "[Ljava.lang.Long;",
            "[Ljava.lang.Byte;",
            "[Ljava.lang.Short;",
            "[Ljava.lang.Float;",
            "[Ljava.lang.Double;",
            "[Ljava.lang.Character;",
            "[Ljava.lang.Boolean;",
            "[Ljava.lang.String;",
            "[Ljava.math.BigInteger;",
            "[Ljava.math.BigDecimal;"
    ));

    private static boolean isPrimitiveCollection(final PersistenceTypeDefinitionMember member)
    {
        return PRIMITIVE_COLLECTIONS.contains(member.typeName());
    }

    private static boolean isCollection(final PersistenceTypeDefinitionMember member)
    {
        // Will be handled as Primitive Collection when we export and identify the type of the item is a Primitive Wrapper.
        return "java.util.List".equals(member.typeName()) ||
                "java.util.Set".equals(member.typeName());
    }

    private static boolean isDictionary(final PersistenceTypeDefinitionMember member)
    {
        // Will be handled as Primitive Collection when we export and identify the type of the item is a Primitive Wrapper.
        return "java.util.Map".equals(member.typeName());
    }

    private static boolean isReference(final PersistenceTypeDefinitionMember member)
    {
        return member.isReference();

    }

    private static boolean isComplex(final PersistenceTypeDefinitionMember member)
    {
        return "[list]".equals(member.typeName());

    }

    private static boolean isEnumArray(final PersistenceTypeDefinitionMember member, final Storage storage)
    {
        final String typeName = member.typeName();
        if (typeName.startsWith("[L") && typeName.endsWith(";"))
        {
            final String className = typeName.substring(2, typeName.length() - 1);
            return storage.isEnumClass(className);
        }

        return false;

    }

    private static boolean isArray(final PersistenceTypeDefinitionMember member)
    {
        final String typeName = member.typeName();
        return "[char]".equals(typeName) || "[byte]".equals(typeName);

    }

    private static boolean isString(final PersistenceTypeDefinitionMember member)
    {
        return "java.lang.String".equals(member.typeName());

    }

    private static boolean isPrimitive(final PersistenceTypeDefinitionMember member)
    {
        return member.isFixedLength() && member.isPrimitive();

    }

    private static boolean isEnum(final PersistenceTypeDefinitionMember member, Storage storage)
    {
        return member.typeName()
                .startsWith("enum") ||
                storage.isEnumClass(member.typeName());

    }

    private static final Set<String> PRIMITIVE_WRAPPERS = new HashSet<>(Arrays.asList(
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Float",
            "java.lang.Double",
            "java.lang.Boolean",
            "java.lang.Character",
            "java.lang.Byte",
            "java.lang.Short",
            "java.math.BigInteger",
            "java.math.BigDecimal"
    ));

    private static boolean isPrimitiveWrapper(final PersistenceTypeDefinitionMember member)
    {
        return PRIMITIVE_WRAPPERS.contains(member.typeName());
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
