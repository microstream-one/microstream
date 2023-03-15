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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * An implementation of an empty result set, no data available. For use in the MetaData implementation class only.
 */
public class EmptyResultSet extends AbstractMicrostreamResultSet
{

    // FIXME Review each method to verify if we do the correct thing.

    @Override
    public boolean next() throws SQLException
    {
        // Always false since we have no data
        return false;
    }

    @Override
    public void close() throws SQLException
    {
        // No Op
    }

    @Override
    public boolean wasNull() throws SQLException
    {
        return false;
    }

    @Override
    public String getString(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public short getShort(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public int getInt(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public long getLong(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public String getString(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public short getShort(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public int getInt(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public long getLong(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public float getFloat(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public double getDouble(String columnLabel) throws SQLException
    {
        return 0;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public Date getDate(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public Time getTime(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        // TODO Implement
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException
    {
        // TODO Implement
    }

    @Override
    public String getCursorName() throws SQLException
    {
        // TODO Needed??
        return null;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException
    {
        // FIXME
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public Object getObject(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public int findColumn(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public boolean isBeforeFirst() throws SQLException
    {
        return true;
    }

    @Override
    public boolean isAfterLast() throws SQLException
    {
        return true;
    }

    @Override
    public boolean isFirst() throws SQLException
    {
        return true; // Actually there is none
    }

    @Override
    public boolean isLast() throws SQLException
    {
        return true; // Actually there is none
    }

    @Override
    public void beforeFirst() throws SQLException
    {
        // Can be no-op when no rows
    }

    @Override
    public void afterLast() throws SQLException
    {
        // Can be no-op when no rows
    }

    @Override
    public boolean first() throws SQLException
    {
        return false;  // No rows mean we must return false
    }

    @Override
    public boolean last() throws SQLException
    {
        return false;// No rows mean we must return false
    }

    @Override
    public int getRow() throws SQLException
    {
        return 0;  // 0 means no rows since numbering start from 1.
    }

    @Override
    public boolean absolute(int row) throws SQLException
    {
        return false;  // return false as no movement done
    }

    @Override
    public boolean relative(int rows) throws SQLException
    {
        return false;  // False as we are not on a row.
    }

    @Override
    public boolean previous() throws SQLException
    {
        return false;// False as we are not on a row.
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException
    {
        // Can be ignored here.
    }

    @Override
    public int getFetchDirection() throws SQLException
    {
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException
    {
        // Can be ignored
    }

    @Override
    public int getFetchSize() throws SQLException
    {
        return 0;
    }

    @Override
    public int getType() throws SQLException
    {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public int getConcurrency() throws SQLException
    {
        return ResultSet.CONCUR_READ_ONLY;
    }


    @Override
    public Statement getStatement() throws SQLException
    {
        // Null is allowed, especially since this class is used with DatabaseMetaData.
        return null;
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public Array getArray(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public URL getURL(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public URL getURL(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }


    @Override
    public int getHoldability() throws SQLException
    {
        // TODO ??
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException
    {
        return false;
    }


    @Override
    public NClob getNClob(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }


    @Override
    public NClob getNClob(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }


    @Override
    public String getNString(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public String getNString(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }


    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnIndex);

    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException
    {
        // No data
        throw new InvalidColumnException(columnLabel);

    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {

        // FIXME
        return null;

    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return false;
    }
}
