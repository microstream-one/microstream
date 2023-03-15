package one.microstream.experimental.jdbc.metadata;

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

import one.microstream.experimental.jdbc.data.JDBCReadingContext;
import one.microstream.experimental.jdbc.resultset.DataResultSet;
import one.microstream.experimental.jdbc.resultset.Field;
import one.microstream.experimental.jdbc.resultset.FieldMetaData;
import one.microstream.experimental.jdbc.resultset.Row;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Crates a ResultSet for the 'tables' within MicroStream stored data.
 */
public final class TableResultSetSupplier implements Supplier<ResultSet>
{


    private final JDBCReadingContext jdbcReadingContext;

    public TableResultSetSupplier(final JDBCReadingContext jdbcReadingContext)
    {

        this.jdbcReadingContext = jdbcReadingContext;
    }

    @Override
    public ResultSet get()
    {

        List<FieldMetaData> fields = defineFields();

        List<Row> data = jdbcReadingContext.getTables()
                .getTableNames()
                .stream()
                .map(name -> createRow(name, fields))
                .collect(Collectors.toList());

        return new DataResultSet(data, "TABLES");
    }

    private Row createRow(String tableName, List<FieldMetaData> fields)
    {
        Row result = new Row();
        for (FieldMetaData metaData : fields)
        {
            String value = null;
            switch (metaData.getName())
            {
                case "TABLE_NAME":
                    value = tableName;
                    break;
                case "TABLE_TYPE":
                    value = "TABLE";
                    break;
                default:
                    // nothing to do, null is the value
            }
            result.addField(new Field(metaData, value));
        }
        return result;
    }

    private List<FieldMetaData> defineFields()
    {
        List<FieldMetaData> result = new ArrayList<>();
        result.add(FieldMetaData.of("TABLE_CAT", String.class)); // TABLE_CAT String => table catalog (may be null)
        result.add(FieldMetaData.of("TABLE_SCHEM", String.class)); // TABLE_SCHEM String => table schema (may be null)
        result.add(FieldMetaData.of("TABLE_NAME", String.class)); // TABLE_NAME String => table name
        result.add(FieldMetaData.of("TABLE_TYPE", String.class)); // TABLE_TYPE String => table type. Typical types are "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
        result.add(FieldMetaData.of("REMARKS", String.class)); // REMARKS String => explanatory comment on the table (may be null)
        result.add(FieldMetaData.of("TYPE_CAT", String.class)); // TYPE_CAT String => the types catalog (may be null)
        result.add(FieldMetaData.of("TYPE_SCHEM", String.class)); // TYPE_SCHEM String => the types schema (may be null)
        result.add(FieldMetaData.of("TYPE_NAME", String.class)); // TYPE_NAME String => type name (may be null)
        result.add(FieldMetaData.of("SELF_REFERENCING_COL_NAME", String.class)); // SELF_REFERENCING_COL_NAME String => name of the designated "identifier" column of a typed table (may be null)
        result.add(FieldMetaData.of("REF_GENERATION", String.class)); // REF_GENERATION String => specifies how values in SELF_REFERENCING_COL_NAME are created. Values are "SYSTEM", "USER", "DERIVED". (may be null)
        return result;
    }
}
