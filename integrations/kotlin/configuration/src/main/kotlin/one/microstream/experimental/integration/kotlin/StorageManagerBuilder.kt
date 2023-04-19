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

import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfiguration
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfigurationBuilder
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation
import one.microstream.storage.types.StorageManager

class StorageManagerBuilder {

    private var root: Any? = null
    private var configurationFile: String? = null
    private val configValues = mutableMapOf<String, String?>()
    private val customizers = mutableListOf<EmbeddedStorageFoundationCustomizer>()
    private val initializers = mutableListOf<StorageManagerInitializer>()

    private val propertiesProviders = mutableSetOf<ConfigurationPropertiesProvider>()

    fun withRoot(root: Any) = apply {this.root = root}

    fun usingConfigurationFile(configurationFile: String): StorageManagerBuilder {
        this.configurationFile = configurationFile
        return this
    }

    fun withFileStorageSystem(): FileStorageSystemBuilder {
        return FileStorageSystemBuilder(this)
    }

    fun withCustomizers(vararg customizers: EmbeddedStorageFoundationCustomizer): StorageManagerBuilder {
        this.customizers.addAll(customizers)
        return this
    }

    fun withInitializers(vararg initializers: StorageManagerInitializer): StorageManagerBuilder {
        this.initializers.addAll(initializers)
        return this
    }

    fun registerPropertiesProvider(provider : ConfigurationPropertiesProvider) {
        propertiesProviders.add(provider)
    }

    fun build(): StorageManager {
        val storageFoundation = buildFoundation()
        val storageManager = storageFoundation.createEmbeddedStorageManager()
        initializers.forEach { it.initialize(storageManager) }

        return storageManager
    }

    fun buildFoundation(): EmbeddedStorageFoundation<*> {
        val builder = if (configurationFile == null) {
            EmbeddedStorageConfigurationBuilder.New()
        } else {
            EmbeddedStorageConfiguration.load(configurationFile)
        }

        propertiesProviders.forEach { configValues.putAll(it.retrieveProperties()) }
        // Set the configuration values that are assigned through builder functions
        configValues.forEach { (key: String, value: String?) ->
            if (value != null) {
                builder[key] = value
            }
        }

        val storageFoundation = builder.createEmbeddedStorageFoundation()
        if (root != null) {
            storageFoundation.setRoot(root)
        }
        customizers.forEach { it.customize(storageFoundation) }
        return storageFoundation
    }

    fun buildAndStart(): StorageManager {

        val storageFoundation = buildFoundation()
        val storageManager = storageFoundation.createEmbeddedStorageManager()
        storageManager.start()

        initializers.forEach { it.initialize(storageManager) }

        return storageManager
    }
}
