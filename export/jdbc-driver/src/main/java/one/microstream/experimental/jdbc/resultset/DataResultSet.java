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


import one.microstream.experimental.jdbc.SQLState;
import one.microstream.experimental.jdbc.exception.InvalidColumnException;
import one.microstream.experimental.jdbc.exception.SQLStateException;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ResultSet} for MicroStream data
 */
public class DataResultSet extends AbstractMicrostreamResultSet
{

    private final List<Row> data;
    private final String tableName;

    private int idx;
    private final int size;

    // The current row
    private List<Field> row;

    private boolean lastNull;

    public DataResultSet(final List<Row> data, final String tableName)
    {
        // FIXME Should be immutable constructs (List and LinkedHashMap)
        this.data = data;
        this.tableName = tableName;
        idx = -1;
        size = data.size();
    }

    public DataResultSet(final List<FieldMetaData> fieldMetaData, Void... toBeatErasure)
    {
        // When there are no results but we want to have metadata for the empty ResultSet
        // FIXME Implement.
        data = new ArrayList<>();
        idx = -1;
        size = 0;
        this.tableName = "FIXME";
    }


    @Override
    public boolean next() throws SQLException
    {
        row = null;
        if (idx < size)
        {
            idx++;
        }
        if (idx < size)
        {
            row = data.get(idx)
                    .getFields();

        }

        return idx < size;
    }

    @Override
    public void close() throws SQLException
    {
        // FIXME
    }

    @Override
    public boolean wasNull() throws SQLException
    {

        return lastNull;
    }

    private void checkColumnIndex(final int columnIndex) throws InvalidColumnException
    {
        if (columnIndex < 1 || columnIndex > row.size())
        {
            throw new InvalidColumnException(columnIndex);
        }
    }

    private void checkColumnIndex(final int columnIndex, final String columLabel) throws InvalidColumnException
    {
        if (columnIndex < 1 || columnIndex > row.size())
        {
            throw new InvalidColumnException(columLabel);
        }
    }

    @Override
    public String getString(final int columnIndex) throws SQLException
    {
        if (row == null)
        {
            // FIXME Is this correct. if we moved past the end, should we return null or exception?
            return null;
        }
        checkColumnIndex(columnIndex);
        Object value = row.get(columnIndex - 1)
                .getValue();
        lastNull = value == null;
        return lastNull ? null : value.toString();

    }

    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException
    {
        lastNull = true;
        //when we have for example an Int with value 0.
        if (row == null)
        {
            return false;  // This is equivalent with SQL NULL
        }
        checkColumnIndex(columnIndex);
        Object value = row.get(columnIndex - 1)
                .getValue();
        if (value == null)
        {
            return false;
        }
        lastNull = false;
        // FIXME Not according to the Javadoc. Some specific convertion is needed
        if (!Boolean.class.isAssignableFrom(value.getClass()))
        {
            throw new SQLStateException(SQLState._2200G, Boolean.class);
        }
        return (boolean) value;


    }

    @Override
    public byte getByte(final int columnIndex) throws SQLException
    {
        lastNull = true;
        if (row == null)
        {

            return 0;  // This is equivalent with SQL NULL
        }
        checkColumnIndex(columnIndex);
        Object value = row.get(columnIndex - 1)
                .getValue();
        if (value == null)
        {
            return 0;
        }

        lastNull = false;
        // FIXME Not according to the Javadoc. Some specific convertion is needed
        //when we have for example an Int with value 0.
        if (!Byte.class.isAssignableFrom(value.getClass()))
        {
            throw new SQLStateException(SQLState._2200G, Byte.class);
        }
        return (byte) value;
    }

    @Override
    public short getShort(final int columnIndex) throws SQLException
    {
        lastNull = true;
        if (row == null)
        {

            return 0;  // This is equivalent with SQL NULL
        }
        checkColumnIndex(columnIndex);
        Object value = row.get(columnIndex - 1)
                .getValue();
        if (value == null)
        {
            return 0;
        }
        lastNull = false;
        // FIXME Not according to the Javadoc. Some specific convertion is needed
        //when we have for example an Int with value 0.
        if (!Short.class.isAssignableFrom(value.getClass()))
        {
            throw new SQLStateException(SQLState._2200G, Short.class);
        }
        return (short) value;
    }

    @Override
    public int getInt(final int columnIndex) throws SQLException
    {
        lastNull = true;
        if (row == null)
        {

            return 0;  // This is equivalent with SQL NULL
        }

        checkColumnIndex(columnIndex);
        Object value = row.get(columnIndex - 1)
                .getValue();
        if (value == null)
        {
            return 0;
        }
        lastNull = false;
        // FIXME Not according to the Javadoc. Some specific convertion is needed
        //when we have for example an Int with value 0.
        if (!Integer.class.isAssignableFrom(value.getClass()))
        {
            throw new SQLStateException(SQLState._2200G, Integer.class);
        }
        return (int) value;
    }

    @Override
    public long getLong(final int columnIndex) throws SQLException
    {
        lastNull = true;
        if (row == null)
        {

            return 0;  // This is equivalent with SQL NULL
        }
        checkColumnIndex(columnIndex);
        Object value = row.get(columnIndex - 1)
                .getValue();
        if (value == null)
        {
            return 0;
        }
        lastNull = false;
        // FIXME Not according to the Javadoc. Some specific convertion is needed
        //when we have for example an Int with value 0.
        if (!Long.class.isAssignableFrom(value.getClass()))
        {
            throw new SQLStateException(SQLState._2200G, Long.class);
        }
        return (long) value;
    }

    @Override
    public float getFloat(final int columnIndex) throws SQLException
    {
        lastNull = true;
        if (row == null)
        {

            return 0.0F;  // This is equivalent with SQL NULL
        }
        checkColumnIndex(columnIndex);
        Object value = row.get(columnIndex - 1)
                .getValue();
        if (value == null)
        {
            return 0.0F;
        }
        lastNull = false;
        // FIXME Not according to the Javadoc. Some specific convertion is needed
        //when we have for example an Int with value 0.
        if (!Float.class.isAssignableFrom(value.getClass()))
        {
            throw new SQLStateException(SQLState._2200G, Float.class);
        }
        return (float) value;
    }

    @Override
    public double getDouble(final int columnIndex) throws SQLException
    {
        lastNull = true;
        if (row == null)
        {
            return 0.0;
        }
        checkColumnIndex(columnIndex);
        Object value = row.get(columnIndex - 1)
                .getValue();
        if (value == null)
        {
            return 0;
        }
        lastNull = false;
        if (Float.class.isAssignableFrom(value.getClass()))
        {
            return (float) value;
        }
        if (Double.class.isAssignableFrom(value.getClass()))
        {
            return (double) value;
        }
        // FIXME
        throw new RuntimeException("Wrong type");

    }

    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException
    {
        return null;
    }

    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException
    {
        return new byte[0];
    }

    @Override
    public Date getDate(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public Time getTime(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public String getString(final String columnLabel) throws SQLException
    {
        lastNull = true;
        if (row == null)
        {
            return null;
        }

        int columnIndex = findColumn(columnLabel);
        checkColumnIndex(columnIndex, columnLabel);
        Object value = row.get(columnIndex - 1)
                .getValue();

        lastNull = value == null;
        return lastNull ? null : value.toString();
    }

    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException
    {
        lastNull = true;
        if (row == null)
        {
            return false;
        }

        int columnIndex = findColumn(columnLabel);
        checkColumnIndex(columnIndex, columnLabel);
        Object value = row.get(columnIndex - 1)
                .getValue();
        if (value == null)
        {
            return false;
        }

        lastNull = false;
        if (!Boolean.class.isAssignableFrom(value.getClass()))
        {
            throw new SQLStateException(SQLState._2200G, Boolean.class);
        }
        return (boolean) value;
    }

    @Override
    public byte getByte(final String columnLabel) throws SQLException
    {
        lastNull = true;
        if (row == null)
        {
            return 0;
        }

        int columnIndex = findColumn(columnLabel);
        checkColumnIndex(columnIndex, columnLabel);
        Object value = row.get(columnIndex - 1)
                .getValue();
        if (value == null)
        {
            return 0;
        }

        lastNull = false;
        if (!Byte.class.isAssignableFrom(value.getClass()))
        {
            throw new SQLStateException(SQLState._2200G, Byte.class);
        }
        return (byte) value;

    }

    @Override
    public short getShort(final String columnLabel) throws SQLException
    {
        lastNull = true;
        if (row == null)
        {
            return 0;
        }

        int columnIndex = findColumn(columnLabel);
        checkColumnIndex(columnIndex, columnLabel);
        Object value = row.get(columnIndex - 1)
                .getValue();
        if (value == null)
        {
            return 0;
        }

        lastNull = false;
        if (!Short.class.isAssignableFrom(value.getClass()))
        {
            throw new SQLStateException(SQLState._2200G, Short.class);
        }
        return (short) value;

    }

    @Override
    public int getInt(final String columnLabel) throws SQLException
    {
        lastNull = true;
        if (row == null)
        {
            return 0;
        }

        int columnIndex = findColumn(columnLabel);
        checkColumnIndex(columnIndex, columnLabel);
        Object value = row.get(columnIndex - 1)
                .getValue();
        if (value == null)
        {
            return 0;
        }

        lastNull = false;
        if (!Integer.class.isAssignableFrom(value.getClass()))
        {
            throw new SQLStateException(SQLState._2200G, Integer.class);
        }
        return (int) value;
    }

    @Override
    public long getLong(final String columnLabel) throws SQLException
    {
        lastNull = true;
        if (row == null)
        {
            return 0;
        }

        int columnIndex = findColumn(columnLabel);
        checkColumnIndex(columnIndex, columnLabel);
        Object value = row.get(columnIndex - 1)
                .getValue();
        if (value == null)
        {
            return 0;
        }

        lastNull = false;
        if (!Long.class.isAssignableFrom(value.getClass()))
        {
            throw new SQLStateException(SQLState._2200G, Long.class);
        }
        return (long) value;
    }

    @Override
    public float getFloat(final String columnLabel) throws SQLException
    {
        lastNull = true;
        if (row == null)
        {
            return 0.0F;
        }

        int columnIndex = findColumn(columnLabel);
        checkColumnIndex(columnIndex, columnLabel);
        Object value = row.get(columnIndex - 1)
                .getValue();
        if (value == null)
        {
            return 0.0F;
        }

        lastNull = false;
        if (!Float.class.isAssignableFrom(value.getClass()))
        {
            throw new SQLStateException(SQLState._2200G, Float.class);
        }
        return (float) value;
    }

    @Override
    public double getDouble(final String columnLabel) throws SQLException
    {
        lastNull = true;
        if (row == null)
        {
            return 0.0;
        }

        int columnIndex = findColumn(columnLabel);
        checkColumnIndex(columnIndex, columnLabel);
        Object value = row.get(columnIndex - 1)
                .getValue();
        if (value == null)
        {
            return 0.0;
        }

        lastNull = false;
        if (!Double.class.isAssignableFrom(value.getClass()))
        {
            throw new SQLStateException(SQLState._2200G, Double.class);
        }
        return (double) value;
    }

    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException
    {
        return null;
    }

    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException
    {
        return new byte[0];
    }

    @Override
    public Date getDate(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public Time getTime(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public InputStream getAsciiStream(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public InputStream getUnicodeStream(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException
    {

    }

    @Override
    public String getCursorName() throws SQLException
    {
        return null;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException
    {
        List<FieldMetaData> metaData = null;
        if (data.size() > 0)
        {
            metaData = data.get(0)
                    .getFields()
                    .stream()
                    .map(Field::getMetaData)
                    .collect(Collectors.toList());
        }
        // FIXME When we have no results
        return new MicrostreamResultSetMetaData(metaData, tableName);
    }

    @Override
    public Object getObject(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public Object getObject(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public int findColumn(final String columnLabel) throws SQLException
    {
        Optional<Field> field = row.stream()
                .filter(f -> f.getMetaData()
                        .getName()
                        .equals(columnLabel))
                .findAny();
        return field.map(f -> row.indexOf(f))
                .orElse(-1) + 1;  // IndexOf is 0 based, JDBC index is 1 based
    }

    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public Reader getCharacterStream(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public boolean isBeforeFirst() throws SQLException
    {
        return false;
    }

    @Override
    public boolean isAfterLast() throws SQLException
    {
        return false;
    }

    @Override
    public boolean isFirst() throws SQLException
    {
        return false;
    }

    @Override
    public boolean isLast() throws SQLException
    {
        return false;
    }

    @Override
    public void beforeFirst() throws SQLException
    {

    }

    @Override
    public void afterLast() throws SQLException
    {

    }

    @Override
    public boolean first() throws SQLException
    {
        return false;
    }

    @Override
    public boolean last() throws SQLException
    {
        return false;
    }

    @Override
    public int getRow() throws SQLException
    {
        return 0;
    }

    @Override
    public boolean absolute(final int row) throws SQLException
    {
        return false;
    }

    @Override
    public boolean relative(final int rows) throws SQLException
    {
        return false;
    }

    @Override
    public boolean previous() throws SQLException
    {
        return false;
    }

    @Override
    public void setFetchDirection(final int direction) throws SQLException
    {

    }

    @Override
    public int getFetchDirection() throws SQLException
    {
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public void setFetchSize(final int rows) throws SQLException
    {

    }

    @Override
    public int getFetchSize() throws SQLException
    {
        return 0;
    }

    @Override
    public int getType() throws SQLException
    {
        return 0;  // TODO
    }

    @Override
    public int getConcurrency() throws SQLException
    {
        return ResultSet.CONCUR_READ_ONLY;
    }

    @Override
    public Statement getStatement() throws SQLException
    {
        return null;
    }

    @Override
    public Object getObject(final int columnIndex, final Map<String, Class<?>> map) throws SQLException
    {
        return null;
    }

    @Override
    public Ref getRef(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public Blob getBlob(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public Clob getClob(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public Array getArray(final int columnIndex) throws SQLException
    {

        if (row == null)
        {
            // FIXME Is this correct. if we moved past the end, should we return null or exception?
            return null;
        }
        checkColumnIndex(columnIndex);
        Object value = row.get(columnIndex - 1)
                .getValue();
        lastNull = value == null;
        return new ListArray((List<?>) value);
    }

    @Override
    public Object getObject(final String columnLabel, final Map<String, Class<?>> map) throws SQLException
    {
        return null;
    }

    @Override
    public Ref getRef(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public Blob getBlob(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public Clob getClob(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public Array getArray(final String columnLabel) throws SQLException
    {
        lastNull = true;
        if (row == null)
        {
            return null;
        }

        int columnIndex = findColumn(columnLabel);
        checkColumnIndex(columnIndex, columnLabel);
        Object value = row.get(columnIndex - 1)
                .getValue();

        lastNull = value == null;
        return new ListArray((List<?>) value);
    }

    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException
    {
        return null;
    }

    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException
    {
        return null;
    }

    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException
    {
        return null;
    }

    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException
    {
        return null;
    }

    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException
    {
        return null;
    }

    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException
    {
        return null;
    }

    @Override
    public URL getURL(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public URL getURL(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public RowId getRowId(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public RowId getRowId(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public int getHoldability() throws SQLException
    {
        return ResultSet.HOLD_CURSORS_OVER_COMMIT;
    }

    @Override
    public boolean isClosed() throws SQLException
    {
        return false;
    }

    @Override
    public NClob getNClob(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public NClob getNClob(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public SQLXML getSQLXML(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public String getNString(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public String getNString(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public Reader getNCharacterStream(final int columnIndex) throws SQLException
    {
        return null;
    }

    @Override
    public Reader getNCharacterStream(final String columnLabel) throws SQLException
    {
        return null;
    }

    @Override
    public <T> T getObject(final int columnIndex, final Class<T> type) throws SQLException
    {
        return null;
    }

    @Override
    public <T> T getObject(final String columnLabel, final Class<T> type) throws SQLException
    {
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
