package one.microstream.experimental.jdbc.util;

/*-
 * #%L
 * jdbc-driver
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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

import one.microstream.experimental.binaryread.structure.EntityMemberType;
import one.microstream.experimental.jdbc.exception.JDBCTypeNotDefinedException;

import java.sql.JDBCType;
import java.sql.SQLException;

public final class JDBCTypeUtil
{

    private JDBCTypeUtil()
    {
    }

    public static JDBCType forTypeName(final EntityMemberType entityMemberType, final String className)
            throws SQLException
    {

        JDBCType result = JDBCType.OTHER;

        switch (entityMemberType)
        {

            case STRING:
            case ENUM:
                result = JDBCType.VARCHAR;
                break;
            case PRIMITIVE:
            case PRIMITIVE_WRAPPER:
                if ("long".equals(className) || "java.lang.Long".equals(className))
                {
                    result = JDBCType.BIGINT;
                }
                if ("int".equals(className) || "java.lang.Integer".equals(className))
                {
                    result = JDBCType.INTEGER;
                }
                if ("byte".equals(className) || "java.lang.Byte".equals(className))
                {
                    result = JDBCType.TINYINT;
                }
                if ("short".equals(className) || "java.lang.Short".equals(className))
                {
                    result = JDBCType.SMALLINT;
                }
                if ("boolean".equals(className) || "java.lang.Boolean".equals(className))
                {
                    result = JDBCType.BOOLEAN;
                }
                if ("double".equals(className) || "java.lang.Double".equals(className))
                {
                    result = JDBCType.DOUBLE;
                }
                if ("float".equals(className) || "java.lang.Float".equals(className))
                {
                    result = JDBCType.FLOAT;
                }
                if ("char".equals(className) || "java.lang.Character".equals(className))
                {
                    result = JDBCType.CHAR;
                }

                if (result == JDBCType.OTHER)
                {
                    throw new JDBCTypeNotDefinedException(entityMemberType, className);
                }
                break;
            case OPTIONAL:
                // FIXME
                break;
            case TIMESTAMP_BASED:
                // FIXME
                break;
            case ARRAY:
            case ENUM_ARRAY:
            case PRIMITIVE_COLLECTION:
                result = JDBCType.ARRAY;
                break;
            case REFERENCE:
            case COLLECTION:
            case DICTIONARY:
            case COMPLEX:
                break;
            default:
                throw new JDBCTypeNotDefinedException(entityMemberType, className);

        }

        return result;
    }
}
