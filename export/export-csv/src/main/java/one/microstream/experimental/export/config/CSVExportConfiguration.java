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

import one.microstream.experimental.export.exception.ExportMarkerConfigurationException;

public class CSVExportConfiguration
{

    private final String targetDirectory;

    private final int fileWriterCacheSize;

    private final String valueDelimiter;  // like \t or ,

    private String collectionMarkerStart; // like [ resulting in [item1, item2, item3]
    private String collectionMarkerEnd; // like ] resulting in [item1, item2, item3]

    private String dictionaryMarkersStart; // like { resulting in {[key:value],[foo:bar]}
    private String dictionaryMarkersEnd; // like } resulting in {[key:value],[foo:bar]}

    private final String dictionaryEntryMarker; // like :, see dictionaryMarkers

    private final String stringsQuote; // like "
    private final boolean isLenient; // When determining if collection can be inlined. Do we look only at first item or do we check everything?

    public CSVExportConfiguration(final String targetDirectory, final int fileWriterCacheSize, final String valueDelimiter, final String collectionMarkers, final String dictionaryMarkers, final String dictionaryEntryMarker, final String stringsQuote, final boolean isLenient)
    {
        this.targetDirectory = targetDirectory;
        this.fileWriterCacheSize = fileWriterCacheSize;
        this.valueDelimiter = valueDelimiter;
        defineCollectionMarkers(checkCollectionMarkers(collectionMarkers));
        defineDictionaryMarkers(checkDictionaryMarkers(dictionaryMarkers));
        this.dictionaryEntryMarker = dictionaryEntryMarker;
        this.stringsQuote = stringsQuote;
        this.isLenient = isLenient;
    }

    private void defineCollectionMarkers(final String collectionMarkers)
    {
        int length = collectionMarkers.length();
        collectionMarkerStart = collectionMarkers.substring(0, length / 2);
        collectionMarkerEnd = collectionMarkers.substring(length / 2);
    }

    private String checkCollectionMarkers(final String collectionMarkers)
    {
        if (collectionMarkers == null || collectionMarkers.isEmpty()
                || collectionMarkers.length() % 2 == 1)
        {
            // Should be an even number of characters as it is split in start and end.
            throw new ExportMarkerConfigurationException("Collection marker", collectionMarkers);
        }
        return collectionMarkers;
    }

    private void defineDictionaryMarkers(final String dictionaryMarkers)
    {
        int length = dictionaryMarkers.length();
        dictionaryMarkersStart = dictionaryMarkers.substring(0, length / 2);
        dictionaryMarkersEnd = dictionaryMarkers.substring(length / 2);
    }

    private String checkDictionaryMarkers(final String dictionaryMarkers)
    {
        if (dictionaryMarkers == null || dictionaryMarkers.isEmpty()
                || dictionaryMarkers.length() % 2 == 1)
        {
            // Should be an even number of characters as it is split in start and end.
            throw new ExportMarkerConfigurationException("Dictionary marker", dictionaryMarkers);
        }
        return dictionaryMarkers;
    }

    public String getTargetDirectory()
    {
        return targetDirectory;
    }

    public int getFileWriterCacheSize()
    {
        return fileWriterCacheSize;
    }

    public String getValueDelimiter()
    {
        return valueDelimiter;
    }

    public String getCollectionMarkerStart()
    {
        return collectionMarkerStart;
    }

    public String getCollectionMarkerEnd()
    {
        return collectionMarkerEnd;
    }

    public String getDictionaryMarkersStart()
    {
        return dictionaryMarkersStart;
    }

    public String getDictionaryMarkersEnd()
    {
        return dictionaryMarkersEnd;
    }

    public String getDictionaryEntryMarker()
    {
        return dictionaryEntryMarker;
    }

    public String getStringsQuote()
    {
        return stringsQuote;
    }

    public boolean isLenient()
    {
        return isLenient;
    }
}
