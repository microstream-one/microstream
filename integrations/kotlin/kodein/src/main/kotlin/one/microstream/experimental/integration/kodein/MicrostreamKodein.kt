/*-
 * #%L
 * MicroStream Kodein integration
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
package one.microstream.experimental.integration.kodein

import one.microstream.experimental.integration.kotlin.StorageManagerInitializer
import one.microstream.storage.types.StorageManager
import org.kodein.di.DI
import org.kodein.di.allInstances
import org.kodein.di.bind
import org.kodein.di.singleton
import kotlin.reflect.KClass

/**
 * Functions to create a binding of the Root object of MicroStream as Kodein bean.
 * Within the Bean definition (the trailing lambda) it can be used as
 *
 * <pre>
 * DI {
 *    MicrostreamKodein.bindRoot(this, Root::class, storageManager)
 * }
 * </pre>
 * where `storageManager` can be an instance of StorageManager or a Kotlin lazy reference.
 *
 * When the bean is accessed and the Root object is 'instantiated', Kodean beans of type
 * StorageManagerInitializer are also applied.
 */
object MicrostreamKodein {

    /**
     *
     */
    inline fun <reified T : Any> bindRoot(
        builder: DI.MainBuilder,
        clazz: KClass<T>,
        storageManager: StorageManager
    ) {
        bindRoot(builder, clazz, lazy {storageManager})
    }

    inline fun <reified T : Any> bindRoot(
        builder: DI.MainBuilder,
        clazz: KClass<T>,
        storageManagerProvider: Lazy<StorageManager>
    ) {
        builder.bind {
            builder.singleton {

                val storageManager = storageManagerProvider.value
                var root = storageManager.root()
                if (root == null) {
                    // Use plain java and not Kotlin Reflect as this is the only usage we have.
                    root = clazz.java.getDeclaredConstructor().newInstance()
                    storageManager.setRoot(root)
                    storageManager.storeRoot()

                    val instances: List<StorageManagerInitializer> by di.allInstances()
                    instances.forEach {
                        it.initialize(storageManager)
                    }
                }
                root as T
            }
        }

    }
}
