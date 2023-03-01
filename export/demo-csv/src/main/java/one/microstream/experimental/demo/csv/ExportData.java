package one.microstream.experimental.demo.csv;

/*-
 * #%L
 * demo
 * %%
 * Copyright (C) 2023 MicroStream Software
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

import one.microstream.experimental.binaryread.config.BinaryReadConfig;
import one.microstream.experimental.binaryread.config.BinaryReadConfigBuilder;
import one.microstream.experimental.export.FlatCSV;
import one.microstream.experimental.export.config.CSVExportConfiguration;
import one.microstream.experimental.export.config.CSVExportConfigurationBuilder;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;

public class ExportData {

    public static void main(final String[] args) {
        final EmbeddedStorageFoundation<?> foundation = Config.createStorageFoundation();

        final BinaryReadConfig binaryReadConfig = new BinaryReadConfigBuilder()
                .withStorageFoundation(foundation)
                .build();
        final CSVExportConfiguration exportConfiguration = new CSVExportConfigurationBuilder()
                .withTargetDirectory("csv")
                .build();

        final FlatCSV flatCSV = new FlatCSV(binaryReadConfig, exportConfiguration);
        flatCSV.export();
    }
}
