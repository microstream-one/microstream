package one.microstream.experimental.export.config;

/*-
 * #%L
 * export-flat-csv
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

public class CSVExportConfigurationBuilder
{
    private String targetDirectory;
    private int fileWriterCacheSize = 16;
    private String valueDelimiter = "\t";
    private String collectionMarkers = "[]";
    private String dictionaryMarkers = "{}";
    private String dictionaryEntryMarker = ":";
    private String stringsQuote = "";
    private boolean isLenient = true;
    private final FilteringOptions filteringOptions = new FilteringOptions();

    public CSVExportConfigurationBuilder withTargetDirectory(final String targetDirectory)
    {
        this.targetDirectory = targetDirectory;
        return this;
    }

    public CSVExportConfigurationBuilder withFileWriterCacheSize(final int fileWriterCacheSize)
    {
        this.fileWriterCacheSize = fileWriterCacheSize;
        return this;
    }

    public CSVExportConfigurationBuilder withValueDelimiter(final String valueDelimiter)
    {
        this.valueDelimiter = valueDelimiter;
        return this;
    }

    public CSVExportConfigurationBuilder withCollectionMarkers(final String collectionMarkers)
    {
        this.collectionMarkers = collectionMarkers;
        return this;
    }

    public CSVExportConfigurationBuilder withDictionaryMarkers(final String dictionaryMarkers)
    {
        this.dictionaryMarkers = dictionaryMarkers;
        return this;
    }

    public CSVExportConfigurationBuilder withDictionaryEntryMarker(final String dictionaryEntryMarker)
    {
        this.dictionaryEntryMarker = dictionaryEntryMarker;
        return this;
    }

    public CSVExportConfigurationBuilder withStringsQuote(final String stringsQuote)
    {
        this.stringsQuote = stringsQuote;
        return this;
    }

    public CSVExportConfigurationBuilder withIsLenient(final boolean isLenient)
    {
        this.isLenient = isLenient;
        return this;
    }

    public CSVExportConfigurationBuilder onlyClasses(final String... classNames)
    {
        filteringOptions.addClassNames(classNames);
        return this;
    }

    public CSVExportConfigurationBuilder onlySubTrees(final String... subTrees)
    {
        filteringOptions.addSubTrees(subTrees);
        return this;
    }

    public CSVExportConfigurationBuilder showSubTrees()
    {
        filteringOptions.showSubTrees();
        return this;
    }

    public CSVExportConfiguration build()
    {
        return new CSVExportConfiguration(targetDirectory, fileWriterCacheSize, valueDelimiter, collectionMarkers
                , dictionaryMarkers, dictionaryEntryMarker, stringsQuote, isLenient, filteringOptions);
    }
}
