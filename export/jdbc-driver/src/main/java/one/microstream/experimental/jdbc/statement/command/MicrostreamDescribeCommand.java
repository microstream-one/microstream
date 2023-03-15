package one.microstream.experimental.jdbc.statement.command;

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

import one.microstream.experimental.binaryread.structure.EntityMemberType;
import one.microstream.experimental.jdbc.SQLState;
import one.microstream.experimental.jdbc.data.JDBCReadingContext;
import one.microstream.experimental.jdbc.exception.SQLStateException;
import one.microstream.experimental.jdbc.resultset.DataResultSet;
import one.microstream.experimental.jdbc.resultset.Field;
import one.microstream.experimental.jdbc.resultset.FieldMetaData;
import one.microstream.experimental.jdbc.resultset.Row;
import one.microstream.experimental.jdbc.util.JDBCTypeUtil;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Command for 'DESCRIBE table'.
 */
public class MicrostreamDescribeCommand extends MicrostreamCommand
{

    private static final String FIELD_NAME_COLUMN_NAME = "column_name";
    private static final String FIELD_NAME_DATA_TYPE = "data_type";

    private final String tableName;

    public MicrostreamDescribeCommand(final JDBCReadingContext jdbcReadingContext, final String sql)
    {
        super(jdbcReadingContext);
        tableName = sql;
    }

    @Override
    public CommandType getCommandType()
    {
        return CommandType.DESCRIBE;
    }

    @Override
    public ResultSet executeQuery() throws SQLException
    {
        final PersistenceTypeDefinition tableDefinition = jdbcReadingContext.getTables()
                .getTableDefinition(tableName);

        if (tableDefinition == null)
        {
            throw new SQLStateException(SQLState.HV00R, tableName);
        }

        final List<FieldMetaData> fieldMetaDataList = defineFields();

        final List<Row> data = new ArrayList<>();

        for (PersistenceTypeDefinitionMember instanceMember : tableDefinition.instanceMembers())
        {
            final Row row = new Row();
            for (FieldMetaData fieldMetaData : fieldMetaDataList)
            {
                String value = null;
                if (FIELD_NAME_COLUMN_NAME.equals(fieldMetaData.getName()))
                {
                    value = instanceMember.name();
                }
                if (FIELD_NAME_DATA_TYPE.equals(fieldMetaData.getName()))
                {

                    final EntityMemberType entityMemberType = EntityMemberType.forDefinition(jdbcReadingContext.getReadingContext(), instanceMember);
                    value = JDBCTypeUtil.forTypeName(entityMemberType, instanceMember.typeName())
                            .getName();
                }
                row.addField(new Field(fieldMetaData, value));
            }
            data.add(row);
        }


        return new DataResultSet(data, tableName);
    }

    private List<FieldMetaData> defineFields()
    {
        List<FieldMetaData> result = new ArrayList<>();
        result.add(FieldMetaData.of(FIELD_NAME_COLUMN_NAME, String.class));
        result.add(FieldMetaData.of(FIELD_NAME_DATA_TYPE, String.class));
        return result;
    }
}
