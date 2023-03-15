/*
Database Table Printer
Copyright (C) 2014  Hami Galip Torun

Email: hamitorun@e-fabrika.net
Project Home: https://github.com/htorun/dbtableprinter

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package one.microstream.experimental.demo.jdbc;

/*-
 * #%L
 * demo-jdbc
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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Just a utility to print rows from a given DB table or a
 * <code>ResultSet</code> to standard out, formatted to look
 * like a table with rows and columns with borders.
 *
 */
public class DBTablePrinter {

    /**
     * Default maximum width for text columns
     * (like a <code>VARCHAR</code>) column.
     */
    private static final int DEFAULT_MAX_TEXT_COL_WIDTH = 150;

    /**
     * Column type category for <code>CHAR</code>, <code>VARCHAR</code>
     * and similar text columns.
     */
    public static final int CATEGORY_STRING = 1;

    /**
     * Column type category for <code>TINYINT</code>, <code>SMALLINT</code>,
     * <code>INT</code> and <code>BIGINT</code> columns.
     */
    public static final int CATEGORY_INTEGER = 2;

    /**
     * Column type category for <code>REAL</code>, <code>DOUBLE</code>,
     * and <code>DECIMAL</code> columns.
     */
    public static final int CATEGORY_DOUBLE = 3;

    /**
     * Column type category for date and time related columns like
     * <code>DATE</code>, <code>TIME</code>, <code>TIMESTAMP</code> etc.
     */
    public static final int CATEGORY_DATETIME = 4;

    /**
     * Column type category for <code>BOOLEAN</code> columns.
     */
    public static final int CATEGORY_BOOLEAN = 5;

    /**
     * Column type category for types for which the type name
     * will be printed instead of the content, like <code>BLOB</code>,
     * <code>BINARY</code>, <code>ARRAY</code> etc.
     */
    public static final int CATEGORY_OTHER = 0;

    /**
     * Represents a database table column.
     */
    private static class Column {

        /**
         * Column label.
         */
        private String label;

        /**
         * Generic SQL type of the column as defined in
         * <a target="_blank"
         * href="http://docs.oracle.com/javase/8/docs/api/java/sql/Types.html">
         * java.sql.Types
         * </a>.
         */
        private int type;

        /**
         * Generic SQL type name of the column as defined in
         * <a target="_blank"
         * href="http://docs.oracle.com/javase/8/docs/api/java/sql/Types.html">
         * java.sql.Types
         * </a>.
         */
        private String typeName;

        /**
         * Width of the column that will be adjusted according to column label
         * and values to be printed.
         */
        private int width = 0;

        /**
         * Column values from each row of a <code>ResultSet</code>.
         */
        private List<String> values = new ArrayList<>();

        /**
         * Flag for text justification using <code>String.format</code>.
         * Empty string (<code>""</code>) to justify right,
         * dash (<code>-</code>) to justify left.
         *
         * @see #justifyLeft()
         */
        private String justifyFlag = "";

        /**
         * Column type category. The columns will be categorised according
         * to their column types and specific needs to print them correctly.
         */
        private int typeCategory = 0;

        /**
         * Constructs a new <code>Column</code> with a column label,
         * generic SQL type and type name (as defined in
         * <a target="_blank"
         * href="http://docs.oracle.com/javase/8/docs/api/java/sql/Types.html">
         * java.sql.Types
         * </a>)
         *
         * @param label    Column label or name
         * @param type     Generic SQL type
         * @param typeName Generic SQL type name
         */
        public Column(String label, int type, String typeName) {
            this.label = label;
            this.type = type;
            this.typeName = typeName;
        }

        /**
         * Returns the column label
         *
         * @return Column label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Returns the generic SQL type of the column
         *
         * @return Generic SQL type
         */
        public int getType() {
            return type;
        }

        /**
         * Returns the generic SQL type name of the column
         *
         * @return Generic SQL type name
         */
        public String getTypeName() {
            return typeName;
        }

        /**
         * Returns the width of the column
         *
         * @return Column width
         */
        public int getWidth() {
            return width;
        }

        /**
         * Sets the width of the column to <code>width</code>
         *
         * @param width Width of the column
         */
        public void setWidth(int width) {
            this.width = width;
        }

        /**
         * Adds a <code>String</code> representation (<code>value</code>)
         * of a value to this column object's {@link #values} list.
         * These values will come from each row of a
         * <a target="_blank"
         * href="http://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html">
         * ResultSet
         * </a> of a database query.
         *
         * @param value The column value to add to {@link #values}
         */
        public void addValue(String value) {
            values.add(value);
        }

        /**
         * Returns the column value at row index <code>i</code>.
         * Note that the index starts at 0 so that <code>getValue(0)</code>
         * will get the value for this column from the first row
         * of a <a target="_blank"
         * href="http://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html">
         * ResultSet</a>.
         *
         * @param i The index of the column value to get
         * @return The String representation of the value
         */
        public String getValue(int i) {
            return values.get(i);
        }

        /**
         * Returns the value of the {@link #justifyFlag}. The column
         * values will be printed using <code>String.format</code> and
         * this flag will be used to right or left justify the text.
         *
         * @return The {@link #justifyFlag} of this column
         * @see #justifyLeft()
         */
        public String getJustifyFlag() {
            return justifyFlag;
        }

        /**
         * Sets {@link #justifyFlag} to <code>"-"</code> so that
         * the column value will be left justified when printed with
         * <code>String.format</code>. Typically numbers will be right
         * justified and text will be left justified.
         */
        public void justifyLeft() {
            this.justifyFlag = "-";
        }

        /**
         * Returns the generic SQL type category of the column
         *
         * @return The {@link #typeCategory} of the column
         */
        public int getTypeCategory() {
            return typeCategory;
        }

        /**
         * Sets the {@link #typeCategory} of the column
         *
         * @param typeCategory The type category
         */
        public void setTypeCategory(int typeCategory) {
            this.typeCategory = typeCategory;
        }
    }

    /**
     * Overloaded method to print rows of a <a target="_blank"
     * href="http://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html">
     * ResultSet</a> to standard out using {@link #DEFAULT_MAX_TEXT_COL_WIDTH}
     * to limit the width of text columns.
     *
     * @param rs The <code>ResultSet</code> to print
     */
    public static void printResultSet(ResultSet rs) {
        printResultSet(rs, DEFAULT_MAX_TEXT_COL_WIDTH);
    }

    /**
     * Overloaded method to print rows of a <a target="_blank"
     * href="http://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html">
     * ResultSet</a> to standard out using <code>maxStringColWidth</code>
     * to limit the width of text columns.
     *
     * @param rs                The <code>ResultSet</code> to print
     * @param maxStringColWidth Max. width of text columns
     */
    public static void printResultSet(ResultSet rs, int maxStringColWidth) {
        try {
            if (rs == null) {
                System.err.println("DBTablePrinter Error: Result set is null!");
                return;
            }
            if (rs.isClosed()) {
                System.err.println("DBTablePrinter Error: Result Set is closed!");
                return;
            }
            if (maxStringColWidth < 1) {
                System.err.println("DBTablePrinter Info: Invalid max. varchar column width. Using default!");
                maxStringColWidth = DEFAULT_MAX_TEXT_COL_WIDTH;
            }

            // Get the meta data object of this ResultSet.
            ResultSetMetaData rsmd;
            rsmd = rs.getMetaData();

            // Total number of columns in this ResultSet
            int columnCount = rsmd.getColumnCount();

            // List of Column objects to store each columns of the ResultSet
            // and the String representation of their values.
            List<Column> columns = new ArrayList<>(columnCount);

            // List of table names. Can be more than one if it is a joined
            // table query
            List<String> tableNames = new ArrayList<>(columnCount);

            // Get the columns and their meta data.
            // NOTE: columnIndex for rsmd.getXXX methods STARTS AT 1 NOT 0
            for (int i = 1; i <= columnCount; i++) {
                Column c = new Column(rsmd.getColumnLabel(i),
                        rsmd.getColumnType(i), rsmd.getColumnTypeName(i));
                c.setWidth(c.getLabel().length());
                c.setTypeCategory(whichCategory(c.getType()));
                columns.add(c);

                if (!tableNames.contains(rsmd.getTableName(i))) {
                    tableNames.add(rsmd.getTableName(i));
                }
            }

            // Go through each row, get values of each column and adjust
            // column widths.
            int rowCount = 0;
            while (rs.next()) {

                // NOTE: columnIndex for rs.getXXX methods STARTS AT 1 NOT 0
                for (int i = 0; i < columnCount; i++) {
                    Column c = columns.get(i);
                    String value;
                    int category = c.getTypeCategory();

                    if (category == CATEGORY_OTHER) {

                        // Use generic SQL type name instead of the actual value
                        // for column types BLOB, BINARY etc.
                        value = "(" + c.getTypeName() + ")";

                    } else {
                        value = rs.getString(i + 1) == null ? "NULL" : rs.getString(i + 1);
                    }
                    switch (category) {
                        case CATEGORY_DOUBLE:

                            // For real numbers, format the string value to have 3 digits
                            // after the point. THIS IS TOTALLY ARBITRARY and can be
                            // improved to be CONFIGURABLE.
                            if (!value.equals("NULL")) {
                                Double dValue = rs.getDouble(i + 1);
                                value = String.format("%.3f", dValue);
                            }
                            break;

                        case CATEGORY_STRING:

                            // Left justify the text columns
                            c.justifyLeft();

                            // and apply the width limit
                            if (value.length() > maxStringColWidth) {
                                value = value.substring(0, maxStringColWidth - 3) + "...";
                            }
                            break;
                    }

                    // Adjust the column width
                    c.setWidth(value.length() > c.getWidth() ? value.length() : c.getWidth());
                    c.addValue(value);
                } // END of for loop columnCount
                rowCount++;

            } // END of while (rs.next)

            /*
            At this point we have gone through meta data, get the
            columns and created all Column objects, iterated over the
            ResultSet rows, populated the column values and adjusted
            the column widths.

            We cannot start printing just yet because we have to prepare
            a row separator String.
             */

            // For the fun of it, I will use StringBuilder
            StringBuilder strToPrint = new StringBuilder();
            StringBuilder rowSeparator = new StringBuilder();

            /*
            Prepare column labels to print as well as the row separator.
            It should look something like this:
            +--------+------------+------------+-----------+  (row separator)
            | EMP_NO | BIRTH_DATE | FIRST_NAME | LAST_NAME |  (labels row)
            +--------+------------+------------+-----------+  (row separator)
             */

            // Iterate over columns
            for (Column c : columns) {
                int width = c.getWidth();

                // Center the column label
                String toPrint;
                String name = c.getLabel();
                int diff = width - name.length();

                if ((diff % 2) == 1) {
                    // diff is not divisible by 2, add 1 to width (and diff)
                    // so that we can have equal padding to the left and right
                    // of the column label.
                    width++;
                    diff++;
                    c.setWidth(width);
                }

                int paddingSize = diff / 2; // InteliJ says casting to int is redundant.

                // Cool String repeater code thanks to user102008 at stackoverflow.com
                // (http://tinyurl.com/7x9qtyg) "Simple way to repeat a string in java"
                String padding = new String(new char[paddingSize]).replace("\0", " ");

                toPrint = "| " + padding + name + padding + " ";
                // END centering the column label

                strToPrint.append(toPrint);

                rowSeparator.append("+");
                rowSeparator.append(new String(new char[width + 2]).replace("\0", "-"));
            }

            String lineSeparator = System.getProperty("line.separator");

            // Is this really necessary ??
            lineSeparator = lineSeparator == null ? "\n" : lineSeparator;

            rowSeparator.append("+").append(lineSeparator);

            strToPrint.append("|").append(lineSeparator);
            strToPrint.insert(0, rowSeparator);
            strToPrint.append(rowSeparator);

            StringJoiner sj = new StringJoiner(", ");
            for (String name : tableNames) {
                sj.add(name);
            }

            String info = "Printing " + rowCount;
            info += rowCount > 1 ? " rows from " : " row from ";
            info += tableNames.size() > 1 ? "tables " : "table ";
            info += sj.toString();

            System.out.println(info);

            // Print out the formatted column labels
            System.out.print(strToPrint);

            String format;

            // Print out the rows
            for (int i = 0; i < rowCount; i++) {
                for (Column c : columns) {

                    // This should form a format string like: "%-60s"
                    format = String.format("| %%%s%ds ", c.getJustifyFlag(), c.getWidth());
                    System.out.print(
                            String.format(format, c.getValue(i))
                    );
                }

                System.out.println("|");
                System.out.print(rowSeparator);
            }

            System.out.println();

            /*
                Hopefully this should have printed something like this:
                +--------+------------+------------+-----------+--------+-------------+
                | EMP_NO | BIRTH_DATE | FIRST_NAME | LAST_NAME | GENDER |  HIRE_DATE  |
                +--------+------------+------------+-----------+--------+-------------+
                |  10001 | 1953-09-02 | Georgi     | Facello   | M      |  1986-06-26 |
                +--------+------------+------------+-----------+--------+-------------+
                |  10002 | 1964-06-02 | Bezalel    | Simmel    | F      |  1985-11-21 |
                +--------+------------+------------+-----------+--------+-------------+
             */

        } catch (SQLException e) {
            System.err.println("SQL exception in DBTablePrinter. Message:");
            System.err.println(e.getMessage());
        }
    }

    /**
     * Takes a generic SQL type and returns the category this type
     * belongs to. Types are categorized according to print formatting
     * needs:
     * <p>
     * Integers should not be truncated so column widths should
     * be adjusted without a column width limit. Text columns should be
     * left justified and can be truncated to a max. column width etc...</p>
     * <p>
     * See also: <a target="_blank"
     * href="http://docs.oracle.com/javase/8/docs/api/java/sql/Types.html">
     * java.sql.Types</a>
     *
     * @param type Generic SQL type
     * @return The category this type belongs to
     */
    private static int whichCategory(int type) {
        switch (type) {
            case Types.BIGINT:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return CATEGORY_INTEGER;

            case Types.REAL:
            case Types.DOUBLE:
            case Types.DECIMAL:
                return CATEGORY_DOUBLE;

            case Types.DATE:
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE:
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return CATEGORY_DATETIME;

            case Types.BOOLEAN:
                return CATEGORY_BOOLEAN;

            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CHAR:
            case Types.NCHAR:
                return CATEGORY_STRING;

            default:
                return CATEGORY_OTHER;
        }
    }
}
