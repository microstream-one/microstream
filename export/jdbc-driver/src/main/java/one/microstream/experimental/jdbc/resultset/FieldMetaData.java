package one.microstream.experimental.jdbc.resultset;

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

import java.sql.JDBCType;

/**
 * Keeps the field name and JDBCType together for use in Describe command and Database meta data.
 */
public class FieldMetaData
{

    private final String name;
    private final JDBCType jdbcType;

    public FieldMetaData(final String name, final JDBCType jdbcType)
    {
        this.name = name;
        this.jdbcType = jdbcType;
    }

    public String getName()
    {
        return name;
    }

    public JDBCType getJdbcType()
    {
        return jdbcType;
    }

    public static FieldMetaData of(final String fieldName, final Class<?> fieldType)
    {
        // FIXME Only used in metaData. Do we ned fieldType parameter since always ?
        JDBCType jdbcType = null;
        if (fieldType.equals(String.class))
        {
            jdbcType = JDBCType.VARCHAR;
        }

        // FIXME jdbc == null
        return new FieldMetaData(fieldName, jdbcType);
    }
}
