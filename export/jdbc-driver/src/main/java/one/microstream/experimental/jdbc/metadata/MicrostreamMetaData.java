package one.microstream.experimental.jdbc.metadata;

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

import one.microstream.experimental.jdbc.ConnectionData;
import one.microstream.experimental.jdbc.MicrostreamDriver;
import one.microstream.experimental.jdbc.data.JDBCReadingContext;
import one.microstream.experimental.jdbc.resultset.EmptyResultSet;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

/**
 * Required implementation for JDBC. Absolute minimum to comply with standard for the moment.
 */
public class MicrostreamMetaData implements DatabaseMetaData
{

    private final ConnectionData connectionData;
    private final JDBCReadingContext jdbcReadingContext;

    public MicrostreamMetaData(final ConnectionData connectionData, final JDBCReadingContext jdbcReadingContext)
    {
        this.connectionData = connectionData;
        this.jdbcReadingContext = jdbcReadingContext;
    }

    @Override
    public boolean allProceduresAreCallable() throws SQLException
    {
        return false;
    }

    @Override
    public boolean allTablesAreSelectable() throws SQLException
    {
        return true;
    }

    @Override
    public String getURL() throws SQLException
    {
        return MicrostreamDriver.JDBC_MICROSTREAM + connectionData.getConnectionURL();
    }

    @Override
    public String getUserName() throws SQLException
    {
        // TODO
        return null;
    }

    @Override
    public boolean isReadOnly() throws SQLException
    {
        return true;
    }

    @Override
    public boolean nullsAreSortedHigh() throws SQLException
    {
        // TODO
        return false;
    }

    @Override
    public boolean nullsAreSortedLow() throws SQLException
    {
        // TODO
        return false;
    }

    @Override
    public boolean nullsAreSortedAtStart() throws SQLException
    {
        // TODO
        return false;
    }

    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException
    {
        // TODO
        return false;
    }

    @Override
    public String getDatabaseProductName() throws SQLException
    {
        return "MicroStream";
    }

    @Override
    public String getDatabaseProductVersion() throws SQLException
    {
        // FIXME We don't know the MicroStream version based on the storage
        return getDriverVersion();  // Use the Driver version as alternative.
    }

    @Override
    public String getDriverName() throws SQLException
    {
        return "MicroStream JDBC Driver";
    }

    @Override
    public String getDriverVersion() throws SQLException
    {
        return String.format("%s.%s", getDriverMajorVersion(), getDriverMinorVersion());
    }

    @Override
    public int getDriverMajorVersion()
    {
        return MicrostreamDriver.MICROSTREAM_JDBC_MAJOR;
    }

    @Override
    public int getDriverMinorVersion()
    {
        return MicrostreamDriver.MICROSTREAM_JDBC_MINOR;
    }

    @Override
    public boolean usesLocalFiles() throws SQLException
    {
        return !connectionData.isRemote();
    }

    @Override
    public boolean usesLocalFilePerTable() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException
    {
        return false;
    }

    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException
    {
        return false;
    }

    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException
    {
        return false;
    }

    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException
    {
        return true;
    }

    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
    {
        return false;
    }

    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
    {
        return false;
    }

    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
    {
        return false;
    }

    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
    {
        return false;
    }

    @Override
    public String getIdentifierQuoteString() throws SQLException
    {
        return null;
    }

    @Override
    public String getSQLKeywords() throws SQLException
    {
        // TODO I don't think we have any specific SQL keyword
        return "";
    }

    @Override
    public String getNumericFunctions() throws SQLException
    {
        // We don't have any number function
        return "";
    }

    @Override
    public String getStringFunctions() throws SQLException
    {
        // We don't have any number function
        return "";
    }

    @Override
    public String getSystemFunctions() throws SQLException
    {
        // We don't have any number function
        return "";
    }

    @Override
    public String getTimeDateFunctions() throws SQLException
    {
        // We don't have any number function
        return "";
    }

    @Override
    public String getSearchStringEscape() throws SQLException
    {
        // TODO Do we need this?
        return "\\";
    }

    @Override
    public String getExtraNameCharacters() throws SQLException
    {
        // We use . within table for referring to properties that are collections and become a 'subtable'.
        return ".";
    }

    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException
    {
        // no DDL supported
        return false;
    }

    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException
    {
        // no DDL supported
        return false;
    }

    @Override
    public boolean supportsColumnAliasing() throws SQLException
    {
        return false;
    }

    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsConvert() throws SQLException
    {
        // TODO Maybe we should support this.
        return false;
    }

    @Override
    public boolean supportsConvert(int fromType, int toType) throws SQLException
    {
        // TODO Maybe we should support this.
        return false;
    }

    @Override
    public boolean supportsTableCorrelationNames() throws SQLException
    {
        // We don't support table aliases in queries
        return false;
    }

    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsOrderByUnrelated() throws SQLException
    {
        return true;
    }

    @Override
    public boolean supportsGroupBy() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsGroupByUnrelated() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsLikeEscapeClause() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsMultipleResultSets() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsMultipleTransactions() throws SQLException
    {
        // No transactions at all
        return false;
    }

    @Override
    public boolean supportsNonNullableColumns() throws SQLException
    {
        return true;
    }

    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException
    {
        // ODBC related
        return false;
    }

    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException
    {
        // ODBC related
        return false;
    }

    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException
    {
        // ODBC related
        return false;
    }

    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsANSI92FullSQL() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsOuterJoins() throws SQLException
    {
        // NO JOINS
        return false;
    }

    @Override
    public boolean supportsFullOuterJoins() throws SQLException
    {
        // NO JOINS
        return false;
    }

    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException
    {
        // NO JOINS
        return false;
    }

    @Override
    public String getSchemaTerm() throws SQLException
    {
        // No schema support
        return "";
    }

    @Override
    public String getProcedureTerm() throws SQLException
    {
        // No procedure support
        return "";
    }

    @Override
    public String getCatalogTerm() throws SQLException
    {
        // No catalog support
        return "";
    }

    @Override
    public boolean isCatalogAtStart() throws SQLException
    {
        // Catalog not supported
        return false;
    }

    @Override
    public String getCatalogSeparator() throws SQLException
    {
        // Catalog not supported
        return null;
    }

    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException
    {
        // no DDL Support
        return false;
    }

    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException
    {
        // no Schema Support
        return false;
    }

    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException
    {
        // no Schema Support
        return false;
    }

    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException
    {
        // no Schema Support
        return false;
    }

    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
    {
        // no Schema Support
        return false;
    }

    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException
    {
        // no Catalog Support
        return false;
    }

    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException
    {
        // no Catalog Support
        return false;
    }

    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException
    {
        // no Catalog Support
        return false;
    }

    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException
    {
        // no Catalog Support
        return false;
    }

    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
    {
        // no Catalog Support
        return false;
    }

    @Override
    public boolean supportsPositionedDelete() throws SQLException
    {
        // no Delete Support
        return false;
    }

    @Override
    public boolean supportsPositionedUpdate() throws SQLException
    {
        // no Update Support
        return false;
    }

    @Override
    public boolean supportsSelectForUpdate() throws SQLException
    {
        // no UPDATE Support
        return false;
    }

    @Override
    public boolean supportsStoredProcedures() throws SQLException
    {
        // Not supported
        return false;
    }

    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException
    {
        // TODO Not for POC, maybe later
        return false;
    }

    @Override
    public boolean supportsSubqueriesInExists() throws SQLException
    {
        // TODO Not for POC, maybe later
        return false;
    }

    @Override
    public boolean supportsSubqueriesInIns() throws SQLException
    {
        // No, advanced
        return false;
    }

    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException
    {
        // No, advanced
        return false;
    }

    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException
    {
        // No, advanced
        return false;
    }

    @Override
    public boolean supportsUnion() throws SQLException
    {
        // Not supported
        return false;
    }

    @Override
    public boolean supportsUnionAll() throws SQLException
    {
        // Not supported
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException
    {
        // Commit/Transaction not supported
        return false;
    }

    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException
    {
        // Commit/Transaction not supported
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException
    {
        // Commit/Transaction not supported
        return false;
    }

    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException
    {
        // Commit/Transaction not supported
        return false;
    }

    @Override
    public int getMaxBinaryLiteralLength() throws SQLException
    {
        // 0 means max is not known
        return 0;
    }

    @Override
    public int getMaxCharLiteralLength() throws SQLException
    {
        // 0 means max is not known
        return 0;
    }

    @Override
    public int getMaxColumnNameLength() throws SQLException
    {
        // O means no limit
        return 0;
    }

    @Override
    public int getMaxColumnsInGroupBy() throws SQLException
    {
        // Group by not supported
        return 0;
    }

    @Override
    public int getMaxColumnsInIndex() throws SQLException
    {
        // Index not supported
        return 0;
    }

    @Override
    public int getMaxColumnsInOrderBy() throws SQLException
    {
        // no limit for us?
        return 0;
    }

    @Override
    public int getMaxColumnsInSelect() throws SQLException
    {
        // No limit I guess
        return 0;
    }

    @Override
    public int getMaxColumnsInTable() throws SQLException
    {
        // No limit I guess
        return 0;
    }

    @Override
    public int getMaxConnections() throws SQLException
    {
        // TODO Let start by having 1 connection in POC.
        return 1;
    }

    @Override
    public int getMaxCursorNameLength() throws SQLException
    {
        // NO limit for cursor name
        return 0;
    }

    @Override
    public int getMaxIndexLength() throws SQLException
    {
        // Index is not supported
        return 0;
    }

    @Override
    public int getMaxSchemaNameLength() throws SQLException
    {
        // Schema is not supported
        return 0;
    }

    @Override
    public int getMaxProcedureNameLength() throws SQLException
    {
        // Procedure is not supported
        return 0;
    }

    @Override
    public int getMaxCatalogNameLength() throws SQLException
    {
        // Catalog is not supported
        return 0;
    }

    @Override
    public int getMaxRowSize() throws SQLException
    {
        // No limit for the size in 1 row.
        return 0;
    }

    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
    {
        // TODO POC doesn't support BLOBS, but no limit
        return false;
    }

    @Override
    public int getMaxStatementLength() throws SQLException
    {
        // No limit
        return 0;
    }

    @Override
    public int getMaxStatements() throws SQLException
    {
        // TODO No limit of concurrent statements??
        return 0;
    }

    @Override
    public int getMaxTableNameLength() throws SQLException
    {
        // No limit
        return 0;
    }

    @Override
    public int getMaxTablesInSelect() throws SQLException
    {
        // No limit
        return 0;
    }

    @Override
    public int getMaxUserNameLength() throws SQLException
    {
        // No Limit
        return 0;
    }

    @Override
    public int getDefaultTransactionIsolation() throws SQLException
    {
        // No transaction support
        return 0;
    }

    @Override
    public boolean supportsTransactions() throws SQLException
    {
        // No transaction support
        return false;
    }

    @Override
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException
    {
        // No transaction support
        return false;
    }

    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException
    {
        // No DDL/DML support
        return false;
    }

    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException
    {
        // NO DML support
        return false;
    }

    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException
    {
        // NO DDL support
        return false;
    }

    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException
    {
        // NO DDL support
        return true;
    }

    @Override
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException
    {
        return new EmptyResultSet();
    }

    @Override
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException
    {
        return new EmptyResultSet();
    }

    @Override
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException
    {
        // Fixme take into account the parameters.
        return new TableResultSetSupplier(jdbcReadingContext).get();
    }

    @Override
    public ResultSet getSchemas() throws SQLException
    {
        return new EmptyResultSet();
    }

    @Override
    public ResultSet getCatalogs() throws SQLException
    {
        return new EmptyResultSet();
    }

    @Override
    public ResultSet getTableTypes() throws SQLException
    {
        // FIXME
        return null;
    }

    @Override
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
    {
        // FIXME
        return null;
    }

    @Override
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException
    {
        // FIXME
        return null;
    }

    @Override
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException
    {
        // FIXME
        return null;
    }

    @Override
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException
    {
        return null;
    }

    @Override
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException
    {
        return null;
    }

    @Override
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException
    {
        return null;
    }

    @Override
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException
    {
        return null;
    }

    @Override
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException
    {
        return null;
    }

    @Override
    public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException
    {
        return null;
    }

    @Override
    public ResultSet getTypeInfo() throws SQLException
    {
        return null;
    }

    @Override
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException
    {
        return null;
    }

    @Override
    public boolean supportsResultSetType(int type) throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException
    {
        return false;
    }

    @Override
    public boolean ownUpdatesAreVisible(int type) throws SQLException
    {
        return false;
    }

    @Override
    public boolean ownDeletesAreVisible(int type) throws SQLException
    {
        return false;
    }

    @Override
    public boolean ownInsertsAreVisible(int type) throws SQLException
    {
        return false;
    }

    @Override
    public boolean othersUpdatesAreVisible(int type) throws SQLException
    {
        return false;
    }

    @Override
    public boolean othersDeletesAreVisible(int type) throws SQLException
    {
        return false;
    }

    @Override
    public boolean othersInsertsAreVisible(int type) throws SQLException
    {
        return false;
    }

    @Override
    public boolean updatesAreDetected(int type) throws SQLException
    {
        return false;
    }

    @Override
    public boolean deletesAreDetected(int type) throws SQLException
    {
        return false;
    }

    @Override
    public boolean insertsAreDetected(int type) throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() throws SQLException
    {
        return false;
    }

    @Override
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException
    {
        return null;
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return null;
    }

    @Override
    public boolean supportsSavepoints() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsNamedParameters() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsMultipleOpenResults() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsGetGeneratedKeys() throws SQLException
    {
        return false;
    }

    @Override
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException
    {
        return null;
    }

    @Override
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException
    {
        return null;
    }

    @Override
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException
    {
        return null;
    }

    @Override
    public boolean supportsResultSetHoldability(int holdability) throws SQLException
    {
        return false;
    }

    @Override
    public int getResultSetHoldability() throws SQLException
    {
        return 0;
    }

    @Override
    public int getDatabaseMajorVersion() throws SQLException
    {
        return 0;
    }

    @Override
    public int getDatabaseMinorVersion() throws SQLException
    {
        return 0;
    }

    @Override
    public int getJDBCMajorVersion() throws SQLException
    {
        return 0;
    }

    @Override
    public int getJDBCMinorVersion() throws SQLException
    {
        return 0;
    }

    @Override
    public int getSQLStateType() throws SQLException
    {
        return DatabaseMetaData.sqlStateSQL;  // TODO Implement error codes according to this state.
    }

    @Override
    public boolean locatorsUpdateCopy() throws SQLException
    {
        return false;
    }

    @Override
    public boolean supportsStatementPooling() throws SQLException
    {
        return false;
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException
    {
        return null;
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException
    {
        return null;
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException
    {
        return false;
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException
    {
        return false;
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException
    {
        return null;
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException
    {
        return null;
    }

    @Override
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException
    {
        return null;
    }

    @Override
    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
    {
        return null;
    }

    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException
    {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        return false;
    }
}
