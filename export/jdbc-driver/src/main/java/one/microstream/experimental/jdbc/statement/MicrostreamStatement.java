package one.microstream.experimental.jdbc.statement;

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

import one.microstream.experimental.jdbc.exception.StatementClosedException;
import one.microstream.experimental.jdbc.statement.command.MicrostreamCommand;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * This is for Statement and PreparedStatement of MicroStream.
 */

public class MicrostreamStatement implements PreparedStatement
{

    private MicrostreamCommand command;
    private boolean active;

    public MicrostreamStatement(final MicrostreamCommand command)
    {

        this.command = command;
        active = true;
    }

    public MicrostreamStatement()
    {
        // no command yet.
    }

    private void checkActive() throws StatementClosedException
    {
        if (!active)
        {
            throw new StatementClosedException();
        }
    }

    @Override
    public ResultSet executeQuery() throws SQLException
    {
        checkActive();
        return command.executeQuery();
    }


    @Override
    public int executeUpdate() throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public void setNull(final int parameterIndex, final int sqlType) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setBoolean(final int parameterIndex, final boolean x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setByte(final int parameterIndex, final byte x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setShort(final int parameterIndex, final short x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setInt(final int parameterIndex, final int x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setLong(final int parameterIndex, final long x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException
    {
        // FIXME
    }

    @Override
    public void clearParameters() throws SQLException
    {
        // FIXME
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException
    {
        // FIXME
    }

    @Override
    public boolean execute() throws SQLException
    {
        // FIXME
        return false;
    }

    @Override
    public void addBatch() throws SQLException
    {
        // FIXME
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException
    {
        // FIXME
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException
    {
        // FIXME
        return null;
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException
    {
        // FIXME
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException
    {
        // FIXME
        return null;
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException
    {
        // FIXME
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException
    {
        // FIXME
        return null;
    }

    @Override
    public int executeUpdate(String sql) throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public void close() throws SQLException
    {
        // FIXME
    }

    @Override
    public int getMaxFieldSize() throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException
    {
        // FIXME

    }

    @Override
    public int getMaxRows() throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public void setMaxRows(int max) throws SQLException
    {
        // FIXME
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException
    {
        // FIXME
    }

    @Override
    public int getQueryTimeout() throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException
    {
        // FIXME
    }

    @Override
    public void cancel() throws SQLException
    {
        // FIXME
    }

    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        // FIXME
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException
    {
        // FIXME
    }

    @Override
    public void setCursorName(String name) throws SQLException
    {
        // FIXME
    }

    @Override
    public boolean execute(String sql) throws SQLException
    {
        // FIXME
        return false;
    }

    @Override
    public ResultSet getResultSet() throws SQLException
    {
        // FIXME
        return null;
    }

    @Override
    public int getUpdateCount() throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public boolean getMoreResults() throws SQLException
    {
        // FIXME
        return false;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException
    {
        // FIXME
    }

    @Override
    public int getFetchDirection() throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException
    {
        // FIXME
    }

    @Override
    public int getFetchSize() throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public int getResultSetType() throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public void addBatch(String sql) throws SQLException
    {
        // FIXME
    }

    @Override
    public void clearBatch() throws SQLException
    {
        // FIXME
    }

    @Override
    public int[] executeBatch() throws SQLException
    {
        // FIXME
        return new int[0];
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        // FIXME
        return null;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException
    {
        // FIXME
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException
    {
        // FIXME
        return null;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
    {
        // FIXME
        return false;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException
    {
        // FIXME
        return false;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException
    {
        // FIXME
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException
    {
        // FIXME
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException
    {
        // FIXME
        return false;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException
    {
        // FIXME
    }

    @Override
    public boolean isPoolable() throws SQLException
    {
        // FIXME
        return false;
    }

    @Override
    public void closeOnCompletion() throws SQLException
    {
        // FIXME
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException
    {
        // FIXME
        return false;
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
        // FIXME
        return false;
    }
}
