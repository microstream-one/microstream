package one.microstream.experimental.jdbc;

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

import one.microstream.experimental.binaryread.ReadingContext;
import one.microstream.experimental.binaryread.config.BinaryReadConfig;
import one.microstream.experimental.binaryread.config.BinaryReadConfigBuilder;
import one.microstream.experimental.binaryread.exception.IncorrectConfigurationException;
import one.microstream.experimental.binaryread.storage.DataFiles;
import one.microstream.experimental.binaryread.structure.Storage;
import one.microstream.experimental.jdbc.data.JDBCReadingContext;
import one.microstream.experimental.jdbc.exception.ConnectionClosedException;
import one.microstream.experimental.jdbc.metadata.MicrostreamMetaData;
import one.microstream.experimental.jdbc.statement.MicrostreamStatement;
import one.microstream.experimental.jdbc.statement.StatementFactory;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.StorageDataInventoryFile;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Implementation of the {@code Connection} for MicroStream.
 */
public class MicrostreamConnection implements Connection
{

    private final ConnectionData connectionData;
    private final boolean active;

    private JDBCReadingContext jdbcReadingContext;

    public MicrostreamConnection(final ConnectionData connectionData)
    {

        this.connectionData = connectionData;

        // FIXME This assumes Local modus!!
        createReadingContext();
        active = true;
    }

    private void createReadingContext()
    {
        BinaryReadConfig readConfig = new BinaryReadConfigBuilder()
                .withStorageFoundation(connectionData.getStorageFoundation())
                .build();
        ReadingContext readingContext = new ReadingContext(readConfig);

        Storage storage = createStorage(readingContext);
        readingContext = new ReadingContext(readingContext, storage);
        storage.analyseStorage(readingContext);

        jdbcReadingContext = new JDBCReadingContext(readingContext);
    }

    private Storage createStorage(final ReadingContext readingContext)
    {
        EmbeddedStorageFoundation<?> storageFoundation = readingContext.getBinaryReadConfig()
                .getStorageFoundation();
        PersistenceTypeDictionary typeDictionary = storageFoundation.getConnectionFoundation()
                .getTypeDictionaryProvider()
                .provideTypeDictionary();

        if (typeDictionary.isEmpty())
        {
            throw new IncorrectConfigurationException("Incorrect configuration of the data storage, no TypeDictionary found");
        }
        final List<StorageDataInventoryFile> files = DataFiles.defineDataFiles(storageFoundation);
        return new Storage(files, typeDictionary);
    }


    private void checkActive() throws ConnectionClosedException
    {
        if (!active)
        {
            throw new ConnectionClosedException();
        }
    }

    @Override
    public Statement createStatement() throws SQLException
    {

        checkActive();
        return new MicrostreamStatement();

    }

    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException
    {
        checkActive();
        return StatementFactory.get(jdbcReadingContext, sql);
    }


    @Override
    public CallableStatement prepareCall(final String sql) throws SQLException
    {
        // TODO Not supported??
        return null;
    }

    @Override
    public String nativeSQL(final String sql) throws SQLException
    {
        // TODO Required?
        return null;
    }

    @Override
    public void setAutoCommit(final boolean autoCommit) throws SQLException
    {
        // This is ignored
    }

    @Override
    public boolean getAutoCommit() throws SQLException
    {
        // This is ignored
        return false;
    }

    @Override
    public void commit() throws SQLException
    {
        // This is ignored
    }

    @Override
    public void rollback() throws SQLException
    {
        // This is ignored
    }

    @Override
    public void close() throws SQLException
    {
        // TODO
    }

    @Override
    public boolean isClosed() throws SQLException
    {
        // TODO
        return false;
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException
    {
        return new MicrostreamMetaData(connectionData, jdbcReadingContext);
    }

    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException
    {
        // This is ignored
    }

    @Override
    public boolean isReadOnly() throws SQLException
    {
        // Always readonly
        return true;
    }

    @Override
    public void setCatalog(final String catalog) throws SQLException
    {
        // This is ignored
    }

    @Override
    public String getCatalog() throws SQLException
    {
        // We don't have the notion of catalog in MicroStream
        return null;
    }

    @Override
    public void setTransactionIsolation(final int level) throws SQLException
    {
        // This is ignored
    }

    @Override
    public int getTransactionIsolation() throws SQLException
    {
        // This is ignored
        return 0;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        // TODO
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException
    {
        // TODO
    }

    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException
    {
        // TODO
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException
    {
        // TODO
        return null;
    }

    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException
    {
        // TODO Do we have this?
        return null;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException
    {
        // TODO do we need this or can we return empty map??
        return null;
    }

    @Override
    public void setTypeMap(final Map<String, Class<?>> map) throws SQLException
    {
        // TODO do we need this or can we return empty map??
    }

    @Override
    public void setHoldability(final int holdability) throws SQLException
    {
        // This can be ignored
    }

    @Override
    public int getHoldability() throws SQLException
    {
        // This can be ignored
        return 0;
    }

    @Override
    public Savepoint setSavepoint() throws SQLException
    {
        // This can be ignored
        return null;
    }

    @Override
    public Savepoint setSavepoint(final String name) throws SQLException
    {
        // This can be ignored
        return null;
    }

    @Override
    public void rollback(final Savepoint savepoint) throws SQLException
    {
        // This is not supported, ignore
    }

    @Override
    public void releaseSavepoint(final Savepoint savepoint) throws SQLException
    {
        // This is not supported, ignore
    }

    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException
    {
        // TODO
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException
    {
        // TODO
        return null;
    }

    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException
    {
        // TODO Supported??
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException
    {
        // TODO
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException
    {
        // TODO
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException
    {
        // TODO
        return null;
    }

    @Override
    public Clob createClob() throws SQLException
    {
        // TODO
        return null;
    }

    @Override
    public Blob createBlob() throws SQLException
    {
        // TODO
        return null;
    }

    @Override
    public NClob createNClob() throws SQLException
    {
        // TODO
        return null;
    }

    @Override
    public SQLXML createSQLXML() throws SQLException
    {
        // TODO Do we want to support this??
        return null;
    }

    @Override
    public boolean isValid(final int timeout) throws SQLException
    {
        // TODO
        return true;
    }

    @Override
    public void setClientInfo(final String name, final String value) throws SQLClientInfoException
    {
        // TODO, not really necessary
    }

    @Override
    public void setClientInfo(final Properties properties) throws SQLClientInfoException
    {
        // TODO, not really necessary
    }

    @Override
    public String getClientInfo(final String name) throws SQLException
    {
        // TODO, not really necessary
        return null;
    }

    @Override
    public Properties getClientInfo() throws SQLException
    {
        // TODO, not really necessary
        return null;
    }

    @Override
    public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException
    {
        // TODO
        return null;
    }

    @Override
    public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException
    {
        // TODO
        return null;
    }

    @Override
    public void setSchema(final String schema) throws SQLException
    {
        // Ignored
    }

    @Override
    public String getSchema() throws SQLException
    {
        // Ignored, null is ok to return
        return null;
    }

    @Override
    public void abort(final Executor executor) throws SQLException
    {
        // TODO
    }

    @Override
    public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException
    {
        // TODO
    }

    @Override
    public int getNetworkTimeout() throws SQLException
    {
        // TODO
        return 0;
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException
    {
        // TODO
        return null;
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException
    {
        // TODO
        return false;
    }
}
