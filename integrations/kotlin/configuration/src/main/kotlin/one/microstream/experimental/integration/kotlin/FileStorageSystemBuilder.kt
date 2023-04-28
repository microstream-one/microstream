/*-
 * #%L
 * MicroStream Kotlin Configuration integration
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
package one.microstream.experimental.integration.kotlin

import java.nio.file.Path

class FileStorageSystemBuilder(private val storageManagerBuilder: StorageManagerBuilder) :
    ConfigurationPropertiesProvider {

    private val configValues = mutableMapOf<String, String>()

    init {
        storageManagerBuilder.registerPropertiesProvider(this)
    }

    // TODO We need this also within other 'systems' since storage-directory means also bucket in AWS S3 for example.
    // Make a similar abstract method for
    fun withPath(path: Path) = apply { configValues["storage-directory"] = path.toAbsolutePath().toString() }

    // Idem
    fun withDeletionDirectory(path: Path) = apply { configValues["deletion-directory"] = path.toAbsolutePath().toString() }

    // Idem
    fun withTruncationDirectory(path: Path) = apply { configValues["truncation-directory"] = path.toAbsolutePath().toString() }

    // Idem
    fun withBackupDirectory(path: Path) = apply { configValues["backup-directory"] = path.toAbsolutePath().toString() }

    // Idem
    fun withChannelDirectoryPrefix(prefix: String) = apply { configValues["channel-directory-prefix"] = prefix }

    // Idem
    fun withDataFilePrefix(prefix: String) = apply { configValues["data-file-prefix"] = prefix }

    // Idem
    fun withDataFileSuffix(suffix: String) = apply { configValues["data-file-suffix"] = suffix }

    // Idem
    fun withTransactionFilePrefix(prefix: String) = apply { configValues["transaction-file-prefix"] = prefix }

    // Idem
    fun withTransactionFileSuffix(suffix: String) = apply { configValues["transaction-file-suffix"] = suffix }

    // Idem
    fun withTypeDictionaryFileName(fileName: String) = apply { configValues["type-dictionary-file-name"] = fileName }

    // Idem
    fun withRescuedFileSuffix(suffix: String) = apply { configValues["rescued-file-suffix"] = suffix }

    // Idem
    fun withLockFileName(fileName: String) = apply { configValues["lock-file-name"] = fileName }

    // Idem
    fun withDataFileMinimumSize(size: Long) = apply { configValues["data-file-minimum-size"] = size.toString() }

    // Idem
    fun withDataFileMaximumSize(size: Long) = apply { configValues["data-file-maximum-size"] = size.toString() }

    // Idem
    fun withDataFileMinimumUseRatio(ratio: Double) = apply { configValues["data-file-minimum-use-ratio"] = ratio.toString() }

    // idem
    fun withDataFileCleanupHeadFile(cleanup: Boolean) = apply { configValues["data-file-cleanup-head-file"] = cleanup.toString() }

    fun endSystemConfig(): StorageManagerBuilder = storageManagerBuilder


    override fun retrieveProperties(): Map<String, String> {
        return configValues
    }
}
