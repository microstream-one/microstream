package one.microstream.experimental.jdbc.statement.query;

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

import one.microstream.experimental.jdbc.util.StringUtil;

import java.util.Arrays;

public final class QueryParser
{


    private QueryParser()
    {
    }

    public static ParsedSelect parse(final String sql)
    {
        int fromIdx = StringUtil.indexOfIgnoreCase(sql, "FROM");
        int whereIdx = StringUtil.indexOfIgnoreCase(sql, "WHERE");
        // FIXME We assume FROM is always there for the moment!!
        String fields = sql.substring(0, fromIdx);
        String table;
        if (whereIdx == -1)
        {
            table = sql.substring(fromIdx + 5);// +5 for FROM
        }
        else
        {
            table = sql.substring(fromIdx + 5, whereIdx);  // +5 for FROM
        }
        // FIXME the Where clause.
        return new ParsedSelect(table, Arrays.asList(fields.split(",")));
    }
}
