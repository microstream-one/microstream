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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Common implementation of a ResultSet where we set all update related option
 * to 'not allowed' or 'not supported'.  Many methods result in an Exception if the user
 * tries to make use of it.
 */
public abstract class AbstractMicrostreamResultSet implements ResultSet
{

    @Override
    public boolean rowUpdated() throws SQLException
    {
        return false;  // No update
    }

    @Override
    public boolean rowInserted() throws SQLException
    {
        return false; // No insert
    }

    @Override
    public boolean rowDeleted() throws SQLException
    {
        return false;  // No delete
    }

    @Override
    public void updateNull(final int columnIndex) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBoolean(final int columnIndex, final boolean x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateByte(final int columnIndex, final byte x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateShort(final int columnIndex, final short x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateInt(final int columnIndex, final int x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateLong(final int columnIndex, final long x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateFloat(final int columnIndex, final float x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateDouble(final int columnIndex, final double x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBigDecimal(final int columnIndex, final BigDecimal x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateString(final int columnIndex, final String x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBytes(final int columnIndex, final byte[] x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateDate(final int columnIndex, final Date x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateTime(final int columnIndex, final Time x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateTimestamp(final int columnIndex, final Timestamp x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x, final int length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x, final int length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x, final int length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateObject(final int columnIndex, final Object x, final int scaleOrLength) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateObject(final int columnIndex, final Object x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateNull(final String columnLabel) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBoolean(final String columnLabel, final boolean x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateByte(final String columnLabel, final byte x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateShort(final String columnLabel, final short x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateInt(final String columnLabel, final int x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateLong(final String columnLabel, final long x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateFloat(final String columnLabel, final float x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateDouble(final String columnLabel, final double x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBigDecimal(final String columnLabel, final BigDecimal x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateString(final String columnLabel, final String x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBytes(final String columnLabel, final byte[] x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateDate(final String columnLabel, final Date x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateTime(final String columnLabel, final Time x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateTimestamp(final String columnLabel, final Timestamp x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream x, final int length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream x, final int length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader, final int length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateObject(final String columnLabel, final Object x, final int scaleOrLength) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateObject(final String columnLabel, final Object x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void insertRow() throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateRow() throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void deleteRow() throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void refreshRow() throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void cancelRowUpdates() throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void moveToInsertRow() throws SQLException
    {
        // Can be a no op
    }

    @Override
    public void moveToCurrentRow() throws SQLException
    {
        // Can be a no op
    }

    @Override
    public void updateRef(final int columnIndex, final Ref x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();

    }

    @Override
    public void updateRef(final String columnLabel, final Ref x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBlob(final int columnIndex, final Blob x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBlob(final String columnLabel, final Blob x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateClob(final int columnIndex, final Clob x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateClob(final String columnLabel, final Clob x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateArray(final int columnIndex, final Array x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateArray(final String columnLabel, final Array x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateRowId(final int columnIndex, final RowId x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateRowId(final String columnLabel, final RowId x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateNString(final int columnIndex, final String nString) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateNString(final String columnLabel, final String nString) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateNClob(final int columnIndex, final NClob nClob) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateNClob(final String columnLabel, final NClob nClob) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateSQLXML(final int columnIndex, final SQLXML xmlObject) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateSQLXML(final String columnLabel, final SQLXML xmlObject) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateNCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x, final long length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x, final long length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream x, final long length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream x, final long length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream, final long length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBlob(final String columnLabel, final InputStream inputStream, final long length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateClob(final int columnIndex, final Reader reader, final long length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateClob(final String columnLabel, final Reader reader, final long length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateNClob(final int columnIndex, final Reader reader, final long length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateNClob(final String columnLabel, final Reader reader, final long length) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateNCharacterStream(final String columnLabel, final Reader reader) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream x) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateBlob(final String columnLabel, final InputStream inputStream) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateClob(final int columnIndex, final Reader reader) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateClob(final String columnLabel, final Reader reader) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateNClob(final int columnIndex, final Reader reader) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void updateNClob(final String columnLabel, final Reader reader) throws SQLException
    {
        throw new SQLFeatureNotSupportedException();
    }
}
