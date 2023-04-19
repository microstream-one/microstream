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

    fun withPath(path: Path) = apply { configValues["storage-directory"] = path.toAbsolutePath().toString() }

    fun endSystemConfig(): StorageManagerBuilder = storageManagerBuilder


    override fun retrieveProperties(): Map<String, String> {
        return configValues
    }
}
