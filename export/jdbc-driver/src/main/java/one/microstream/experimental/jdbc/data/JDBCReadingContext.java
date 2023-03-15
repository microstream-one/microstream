package one.microstream.experimental.jdbc.data;

/*-
 * #%L
 * jdbc-driver
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

import one.microstream.experimental.binaryread.ReadingContext;

/**
 * A single holder with all classes required to read data for the JDBC driver.
 */
public class JDBCReadingContext
{

    private final ReadingContext readingContext;
    private final Tables tables;

    public JDBCReadingContext(final ReadingContext readingContext)
    {
        this.readingContext = readingContext;
        this.tables = new Tables(readingContext.getStorage());
    }

    public ReadingContext getReadingContext()
    {
        return readingContext;
    }

    public Tables getTables()
    {
        return tables;
    }
}
