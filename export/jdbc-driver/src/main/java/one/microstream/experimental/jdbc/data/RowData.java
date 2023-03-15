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

import java.util.List;

/**
 * Holds information about rows that are selected to be sent back to the caller. In an intermediate stage,
 * it keeps tracks of the 'rows' when we are 'drilling down' to the actual data that are requested.
 */
public interface RowData
{

    /**
     * Select the data from the current rows by selecting data kept by the 'name' field.
     * @param name Part of table name That we select
     * @return List of current selected rows.
     */
    List<RowData> getSubRowData(final String name);

    /**
     * Get the value of the field in the row or returns null when field name doesn't exist.
     * @param name field for which we want the value.
     * @return value for the field or null.
     */
    Object getValue(final String name);

}
