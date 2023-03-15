package one.microstream.experimental.jdbc.statement;

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

import one.microstream.experimental.jdbc.SQLState;
import one.microstream.experimental.jdbc.data.JDBCReadingContext;
import one.microstream.experimental.jdbc.exception.SQLStateException;
import one.microstream.experimental.jdbc.statement.command.MicrostreamDescribeCommand;
import one.microstream.experimental.jdbc.statement.command.MicrostreamSelectCommand;
import one.microstream.experimental.jdbc.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementFactory
{

    public static final String DESCRIBE = "DESCRIBE ";
    public static final String SELECT = "SELECT ";

    public static PreparedStatement get(final JDBCReadingContext jdbcReadingContext, final String sql)
            throws SQLException
    {
        String trimmed = sql.trim();
        MicrostreamStatement result = null;
        if (StringUtil.startsWithIgnoreCase(trimmed, DESCRIBE))
        {
            result = new MicrostreamStatement(new MicrostreamDescribeCommand(jdbcReadingContext, trimmed.substring(DESCRIBE.length())));
        }
        if (StringUtil.startsWithIgnoreCase(trimmed, SELECT))
        {
            result = new MicrostreamStatement(new MicrostreamSelectCommand(jdbcReadingContext, trimmed.substring(SELECT.length())));
        }
        if (result == null)
        {
            throw new SQLStateException(SQLState._42000);
        }
        return result;
    }
}
