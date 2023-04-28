/*-
 * #%L
 * MicroStream SQL data Storage Configuration integration
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
package one.microstream.experimental.integration.sql.kotlin

import one.microstream.afs.sql.types.SqlDataSourceProvider
import one.microstream.experimental.integration.kotlin.ConfigurationPropertiesProvider
import one.microstream.experimental.integration.kotlin.StorageManagerBuilder
import java.util.function.Supplier
import javax.sql.DataSource

class SQLConfigurationProvider(private val storageManagerBuilder: StorageManagerBuilder) :
    ConfigurationPropertiesProvider {

    init {
        storageManagerBuilder.registerPropertiesProvider(this)
    }

    private var name: String? = null
    private var supplierDefined = false

    // These are not in the format for MicroStream, just the properties that the user wants to pass on to the SqlDataSourceProvider
    private val configValues = mutableMapOf<String, String>()

    private var providerClass: Class<SqlDataSourceProvider>? = null

    fun withDataSourceSupplier(supplier: Supplier<DataSource>) = apply {
        ExternalSqlDataSourceProvider.setDataSourceSupplier(supplier)
        supplierDefined = true
    }

    fun withTablePrefix(prefix: String) = apply { configValues["storage-directory"] = prefix }

    fun withTableDeletionPrefix(prefix: String) = apply { configValues["deletion-directory"] = prefix }

    fun withTableTruncationPrefix(prefix: String) = apply { configValues["truncation-directory"] = prefix }

    fun withTableBackupPrefix(prefix: String) = apply { configValues["backup-directory"] = prefix }

    fun withOptionalChannelPrefix(prefix: String) = apply { configValues["channel-directory-prefix"] = prefix }

    fun withDataFileIdentifierPrefix(prefix: String) = apply { configValues["data-file-prefix"] = prefix }

    fun withDataFileIdentifierSuffix(suffix: String) = apply { configValues["data-file-suffix"] = suffix }

    // TODO Keep these names?
    fun withTransactionFilePrefix(prefix: String) = apply { configValues["transaction-file-prefix"] = prefix }

    // TODO Keep these names?
    fun withTransactionFileSuffix(suffix: String) = apply { configValues["transaction-file-suffix"] = suffix }

    // TODO Keep these names?
    fun withTypeDictionaryFileName(fileName: String) = apply { configValues["type-dictionary-file-name"] = fileName }

    // TODO Keep these names?
    fun withRescuedFileSuffix(suffix: String) = apply { configValues["rescued-file-suffix"] = suffix }

    fun withLockFileIdentifier(fileName: String) = apply { configValues["lock-file-name"] = fileName }

    fun withRecordMinimumSize(size: Long) = apply { configValues["data-file-minimum-size"] = size.toString() }

    fun withRecordMaximumSize(size: Long) = apply { configValues["data-file-maximum-size"] = size.toString() }

    fun withRecordMinimumUseRatio(ratio: Double) =
        apply { configValues["data-file-minimum-use-ratio"] = ratio.toString() }

    fun withCleanupHeadRecord(cleanup: Boolean) =
        apply { configValues["data-file-cleanup-head-file"] = cleanup.toString() }

    fun endSystemConfig(): StorageManagerBuilder {
        return storageManagerBuilder
    }

    fun withDataSourceProviderConfiguration(key: String, value: String) = apply { configValues.put(key, value) }

    override fun retrieveProperties(): Map<String, String> {
        val name = defineName()
        if (providerClass == null && !supplierDefined) {
            throw MissingConfigurationException("ProviderClass or Supplier of DataSource must be provided.")
        }
        if (providerClass != null && supplierDefined) {
            throw MissingConfigurationException("ProviderClass and Supplier of DataSource are provided, only one allowed.")
        }

        val result = mutableMapOf<String, String>()

        if (providerClass != null) {
            result["storage-filesystem.sql.$name.data-source-provider"] = providerClass.toString()
        } else {
            result["storage-filesystem.sql.$name.data-source-provider"] =
                ExternalSqlDataSourceProvider::class.qualifiedName.toString()

        }

        configValues.forEach { (key, value) -> result["storage-filesystem.sql.$name.$key"] = value }

        return result
    }

    private fun defineName(): String {
        return name ?: findFromConfigValues() ?: "default"
    }

    private fun findFromConfigValues(): String? {
        return configValues.values.filter { it.startsWith("jdbc:") }
            .map { it.split(":")[1] }  // TODO We assume correct JDBC URL jdbc:<name>://...
            .firstOrNull()
    }
}
