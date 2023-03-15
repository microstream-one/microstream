package one.microstream.experimental.jdbc.resultset;

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

import java.sql.Array;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of SQL Array for a List of primitives.
 */
public class ListArray implements Array
{
    private List<?> data;
    private final JDBCType jdbcType;

    public ListArray(final List<?> data)
    {
        this.data = data;
        this.jdbcType = defineJDBCType();
    }

    @Override
    public String getBaseTypeName() throws SQLException
    {
        return jdbcType.name();
    }

    @Override
    public int getBaseType() throws SQLException
    {
        return jdbcType.getVendorTypeNumber();
    }

    @Override
    public Object getArray() throws SQLException
    {
        return data.toArray();
    }

    @Override
    public Object getArray(final long index, final int count) throws SQLException
    {
        List<?> subList = data.subList((int) index - 1, (int) index - 1 + count);
        return subList.toArray();
    }

    @Override
    public Object getArray(final long index, final int count, final Map<String, Class<?>> map) throws SQLException
    {
        // Not implemented
        return null;
    }

    @Override
    public Object getArray(final java.util.Map<String, Class<?>> map) throws SQLException
    {
        // Not implemented
        return null;
    }

    @Override
    public ResultSet getResultSet() throws SQLException
    {
        // Not implemented
        return null;
    }

    @Override
    public ResultSet getResultSet(final long index, final int count) throws SQLException
    {
        // Not implemented
        return null;
    }

    @Override
    public ResultSet getResultSet(final long index, final int count, final Map<String, Class<?>> map) throws SQLException
    {
        // Not implemented
        return null;
    }

    @Override
    public ResultSet getResultSet(final Map<String, Class<?>> map) throws SQLException
    {
        // Not implemented
        return null;
    }

    @Override
    public void free() throws SQLException
    {
        data = null;

    }

    private JDBCType defineJDBCType()
    {
        JDBCType result = JDBCType.NULL;
        if (data == null || data.isEmpty())
        {
            return result;
        }
        Optional<?> nonNullItem = data.stream()
                .filter(Objects::nonNull)
                .findAny();
        if (nonNullItem.isEmpty())
        {
            return result;
        }
        Class<?> javaType = nonNullItem.get()
                .getClass();
        switch (javaType.getName())
        {
            case "java.lang.Boolean":
                result = JDBCType.BIT;
                break;
            case "java.lang.Byte":
                result = JDBCType.TINYINT;
                break;
            case "java.lang.Short":
                result = JDBCType.SMALLINT;
                break;
            case "java.lang.Integer":
                result = JDBCType.INTEGER;
                break;
            case "java.lang.Long":
                result = JDBCType.BIGINT;
                break;
            case "java.lang.Float":
                result = JDBCType.FLOAT;
                break;
            case "java.lang.Double":
                result = JDBCType.DOUBLE;
                break;
            case "java.lang.String":
                result = JDBCType.VARCHAR;
                break;
            default:
                result = JDBCType.JAVA_OBJECT;
        }
        return result;
    }
}
