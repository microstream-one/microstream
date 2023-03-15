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
import one.microstream.experimental.binaryread.structure.Storage;
import one.microstream.experimental.jdbc.SQLState;
import one.microstream.experimental.jdbc.data.EntityRowData;
import one.microstream.experimental.jdbc.data.JDBCReadingContext;
import one.microstream.experimental.jdbc.data.RowData;
import one.microstream.experimental.jdbc.exception.SQLStateException;
import one.microstream.experimental.jdbc.resultset.DataResultSet;
import one.microstream.experimental.jdbc.resultset.Field;
import one.microstream.experimental.jdbc.resultset.FieldMetaData;
import one.microstream.experimental.jdbc.resultset.Row;
import one.microstream.experimental.jdbc.statement.query.ParsedSelect;
import one.microstream.experimental.jdbc.statement.query.QueryParser;
import one.microstream.experimental.jdbc.util.JDBCTypeUtil;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of 'SELECT ...' for MicroStream.
 */
public class MicrostreamSelectCommand extends MicrostreamCommand
{


    private final String sql;

    public MicrostreamSelectCommand(final JDBCReadingContext jdbcReadingContext, final String sql)
    {
        super(jdbcReadingContext);
        this.sql = sql;

    }

    @Override
    public CommandType getCommandType()
    {
        return CommandType.SELECT;
    }

    @Override
    public ResultSet executeQuery() throws SQLException
    {
        final ParsedSelect parsedSelect = QueryParser.parse(sql);
        final PersistenceTypeDefinition tableDefinition = jdbcReadingContext.getTables()
                .getTableDefinition(parsedSelect.getTable());

        if (tableDefinition == null)
        {
            throw new SQLStateException(SQLState.HV00R, parsedSelect.getTable());
        }

        final List<PersistenceTypeDefinitionMember> fields = new ArrayList<>();
        if (parsedSelect.isStarSelect())
        {
            for (PersistenceTypeDefinitionMember instanceMember : tableDefinition.instanceMembers())
            {
                //if (instanceMember.isPrimitive()) {
                //String is not a primitive
                // FIXME How to handle the other types? Additional annotation and tool to generate additional info??
                // TODO This additional data could also help accessing the binary data. (like easier to determine root object)
                fields.add(instanceMember);
                //}
            }
        }
        else
        {
            throw new UnsupportedOperationException("Not implemented yet");
        }
        List<RowData> rows = getCollectionFor(parsedSelect.getTable());

        List<FieldMetaData> fieldMetaData = defineTableFields(fields);

        List<Row> data = new ArrayList<>();
        for (RowData rowData : rows)
        {
            Row row = new Row();
            for (FieldMetaData metaData : fieldMetaData)
            {
                row.addField(new Field(metaData, rowData.getValue(metaData.getName())));
            }
            data.add(row);
        }

        return new DataResultSet(data, parsedSelect.getTable());
    }


    public List<RowData> getCollectionFor(final String tableName)
    {

        final List<String> nameParts = new ArrayList<>(Arrays.asList(tableName.split("\\.")));
        // The tableName should be checked and be valid already.

        List<RowData> result = new ArrayList<>();
        while (!nameParts.isEmpty())
        {
            final String namePart = nameParts.remove(0);
            result = getTableData(result, namePart);
        }

        return result;

    }

    private List<RowData> getTableData(final List<RowData> rows, final String fieldName)
    {
        final List<RowData> result = new ArrayList<>();

        final Storage storage = jdbcReadingContext.getReadingContext()
                .getStorage();

        if (rows.isEmpty())
        {
            result.add(new EntityRowData(jdbcReadingContext, storage.getRoot()));
        }
        else
        {
            for (RowData row : rows)
            {
                result.addAll(row.getSubRowData(fieldName));

            }
        }
        return result;
    }


    private List<FieldMetaData> defineTableFields(final List<PersistenceTypeDefinitionMember> fields)
            throws SQLException
    {
        final List<FieldMetaData> result = new ArrayList<>();
        for (PersistenceTypeDefinitionMember field : fields)
        {
            final EntityMemberType entityMemberType = EntityMemberType.forDefinition(jdbcReadingContext.getReadingContext(), field);
            result.add(new FieldMetaData(field.name(), JDBCTypeUtil.forTypeName(entityMemberType, field.typeName())));
        }
        return result;
    }

}
