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


import one.microstream.experimental.jdbc.exception.InvalidColumnException;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementation of {@link ResultSetMetaData} for MicroStream data.
 */
public class MicrostreamResultSetMetaData implements ResultSetMetaData
{

    private final List<FieldMetaData> metaDataList;
    private final String tableName;

    public MicrostreamResultSetMetaData(final List<FieldMetaData> metaDataList, final String tableName)
    {
        this.metaDataList = metaDataList;
        this.tableName = tableName;
    }

    @Override
    public int getColumnCount() throws SQLException
    {
        return metaDataList.size();
    }

    @Override
    public boolean isAutoIncrement(final int column) throws SQLException
    {
        // never
        return false;
    }

    @Override
    public boolean isCaseSensitive(final int column) throws SQLException
    {
        // Since java property names are case sensitive
        return true;
    }

    @Override
    public boolean isSearchable(final int column) throws SQLException
    {
        // Can be in where clause
        return true;
    }

    @Override
    public boolean isCurrency(final int column) throws SQLException
    {
        // FIXME
        return false;
    }

    @Override
    public int isNullable(final int column) throws SQLException
    {
        return ResultSetMetaData.columnNullableUnknown;
    }

    @Override
    public boolean isSigned(final int column) throws SQLException
    {
        // FIXME
        return false;
    }

    @Override
    public int getColumnDisplaySize(final int column) throws SQLException
    {
        // FIXME What is reasonable??
        return 20;
    }

    private void checkColumnIndex(final int column) throws InvalidColumnException
    {
        if (column < 0 || column > metaDataList.size())
        {
            throw new InvalidColumnException(column);
        }
    }

    @Override
    public String getColumnLabel(final int column) throws SQLException
    {
        checkColumnIndex(column);
        // FIXME Support for alias?
        return metaDataList.get(column - 1)
                .getName();
    }

    @Override
    public String getColumnName(final int column) throws SQLException
    {
        checkColumnIndex(column);
        return metaDataList.get(column - 1)
                .getName();
    }

    @Override
    public String getSchemaName(final int column) throws SQLException
    {
        // NOt supported
        return null;
    }

    @Override
    public int getPrecision(final int column) throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public int getScale(final int column) throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public String getTableName(final int column) throws SQLException
    {
        return tableName;
    }

    @Override
    public String getCatalogName(final int column) throws SQLException
    {
        // Not supported
        return null;
    }

    @Override
    public int getColumnType(final int column) throws SQLException
    {
        checkColumnIndex(column);
        return metaDataList.get(column - 1)
                .getJdbcType()
                .getVendorTypeNumber();

    }

    @Override
    public String getColumnTypeName(final int column) throws SQLException
    {
        checkColumnIndex(column);
        return metaDataList.get(column - 1)
                .getJdbcType()
                .getName();
    }

    @Override
    public boolean isReadOnly(final int column) throws SQLException
    {
        // always
        return true;
    }

    @Override
    public boolean isWritable(final int column) throws SQLException
    {
        // never
        return false;
    }

    @Override
    public boolean isDefinitelyWritable(final int column) throws SQLException
    {
        // No never
        return false;
    }

    @Override
    public String getColumnClassName(final int column) throws SQLException
    {
        // FIXME
        return null;
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException
    {
        return null;
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException
    {
        return false;
    }
}
