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
import one.microstream.configuration.types.Configuration
import java.util.function.Supplier
import javax.sql.DataSource

class ExternalSqlDataSourceProvider : SqlDataSourceProvider {

    companion object {
        private var dataSourceSupplier: Supplier<DataSource>? = null

        fun setDataSourceSupplier(supplier: Supplier<DataSource>) {
            this.dataSourceSupplier = supplier
        }
    }

    override fun provideDataSource(configuration: Configuration?): DataSource {
        // TODO
        return dataSourceSupplier?.get() ?: throw UnsupportedOperationException("No Supplier defined.  TODO a custom exception")
    }
}
