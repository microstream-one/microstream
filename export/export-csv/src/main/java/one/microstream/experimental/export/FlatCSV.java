package one.microstream.experimental.export;

/*-
 * #%L
 * export
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

import one.microstream.experimental.binaryread.ReadingContext;
import one.microstream.experimental.binaryread.config.BinaryReadConfig;
import one.microstream.experimental.binaryread.storage.DataFiles;
import one.microstream.experimental.binaryread.structure.Storage;
import one.microstream.experimental.export.config.CSVExportConfiguration;
import one.microstream.experimental.export.exception.NotSupportedException;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.StorageDataInventoryFile;

import java.util.List;

public class FlatCSV {

    private final CSVExportConfiguration csvExportConfiguration;
    private PersistenceTypeDictionary typeDictionary;
    private ReadingContext readingContext;

    public FlatCSV(final BinaryReadConfig binaryReadConfig, final CSVExportConfiguration csvExportConfiguration) {
        if (!Validation.ensureExportDirectoryValidity(csvExportConfiguration.getTargetDirectory())) {
            throw new NotSupportedException(String.format("The directory %s is not empty", csvExportConfiguration.getTargetDirectory()));
        }

        this.csvExportConfiguration = csvExportConfiguration;
        this.readingContext = new ReadingContext(binaryReadConfig);
    }

    private Storage createStorage() {
        EmbeddedStorageFoundation<?> storageFoundation = readingContext.getBinaryReadConfig().getStorageFoundation();
        typeDictionary = storageFoundation.getConnectionFoundation().getTypeDictionaryProvider().provideTypeDictionary();

        final List<StorageDataInventoryFile> files = DataFiles.defineDataFiles(storageFoundation);
        return new Storage( files, typeDictionary);
    }

    public void export() {
        try (final Storage storage = createStorage()) {
            readingContext = new ReadingContext(readingContext, storage);
            storage.analyseStorage(readingContext);
            final DataExporter exporter = new DataExporter(storage, typeDictionary, csvExportConfiguration);

            exporter.export();
        }
    }
}
