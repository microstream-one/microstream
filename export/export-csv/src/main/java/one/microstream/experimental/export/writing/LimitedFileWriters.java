package one.microstream.experimental.export.writing;

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

import one.microstream.experimental.binaryread.exception.UnexpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class LimitedFileWriters
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LimitedFileWriters.class);

    private final int limit;

    private final Map<String, Writer> fileWriterMap = new HashMap<>();

    private final Queue<String> fileWriterUsage = new LinkedList<>();

    private int totalWriterRequests;

    private int cachedWriterRequests;

    public LimitedFileWriters(final int limit)
    {
        this.limit = limit;
    }


    public Writer get(final String fileName, final WriteHeaders writeHeaders)
    {
        totalWriterRequests++;
        Writer result = fileWriterMap.get(fileName);
        if (result == null)
        {
            if (fileWriterMap.size() == limit)
            {
                closeOldestFile();
            }
            result = createFileWriter(fileName, writeHeaders);
            fileWriterMap.put(fileName, result);  // Cache
            fileWriterUsage.add(fileName); // Add to end of queue
        }
        else
        {
            updateFileWriterUsage(fileName);
            cachedWriterRequests++;
        }
        return result;
    }

    private void closeOldestFile()
    {
        final String fileName = fileWriterUsage.remove();
        final Writer writer = fileWriterMap.remove(fileName);
        try
        {
            writer.close();
        } catch (IOException e)
        {
            throw new UnexpectedException("Exception when writing to the file", e);
        }
    }

    private void updateFileWriterUsage(final String fileName)
    {
        fileWriterUsage.remove(fileName);  // remove from queue
        fileWriterUsage.add(fileName); // Add to end of queue
    }

    private FileWriter createFileWriter(final String fileName, final WriteHeaders writeHeaders)
    {
        final File csvFile = new File(fileName);
        final boolean fileExists = csvFile.exists();

        FileWriter result;
        try
        {
            result = new FileWriter(csvFile, true);
        } catch (final IOException e)
        {
            throw new UnexpectedException("Exception when creating the file", e);
        }
        if (!fileExists)
        {
            writeHeaders.write(result);
        }
        return result;
    }

    public void close()
    {
        printCacheEfficiency();
        fileWriterMap.values()
                .forEach(w ->
                         {
                             try
                             {
                                 w.close();
                             } catch (IOException e)
                             {
                                 throw new UnexpectedException("Exception when creating the file", e);
                             }
                         });
    }

    private void printCacheEfficiency()
    {
        double cacheHitPercentage = 100.0 * cachedWriterRequests / totalWriterRequests; // calculate cache hit ratio
        String message = String.format("FileWriter Cache hit ratio: %.2f%%", cacheHitPercentage);
        // No format options for placeholders in SLF4J.
        LOGGER.info(message);

    }
}
