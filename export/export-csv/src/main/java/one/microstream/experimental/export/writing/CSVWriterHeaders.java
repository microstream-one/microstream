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
import one.microstream.experimental.export.config.CSVExportConfiguration;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

import java.io.IOException;
import java.io.Writer;

public class CSVWriterHeaders implements WriteHeaders
{

    private final CSVExportConfiguration csvExportConfiguration;

    private final PersistenceTypeDefinition typeDefinition;

    public CSVWriterHeaders(final CSVExportConfiguration csvExportConfiguration, final PersistenceTypeDefinition typeDefinition)
    {
        this.csvExportConfiguration = csvExportConfiguration;
        this.typeDefinition = typeDefinition;
    }


    @Override
    public void write(final Writer writer)
    {
        final StringBuilder names = new StringBuilder();
        // every object has an id (=MicroStream ObjectId)
        names.append("ObjectId");
        for (final PersistenceTypeDefinitionMember member : typeDefinition.allMembers())
        {
            names.append(csvExportConfiguration.getValueDelimiter())
                    .append(member.name());
        }

        try
        {
            writer.write(names.toString());
            writer.write("\n");
        } catch (final IOException e)
        {
            throw new UnexpectedException("Exception when writing to the file", e);
        }
    }
}
