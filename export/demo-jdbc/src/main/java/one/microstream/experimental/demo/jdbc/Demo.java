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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Demo {
    public static void main(String[] args) throws SQLException {
        // TODO Change the filename for your environment
        Connection conn = DriverManager.getConnection("jdbc:microstream:local:/Users/rubus/code/microstream-experimental/microstream/export/demo-jdbc/src/main/java/demo.ini");

        DatabaseMetaData metaData = conn.getMetaData();

        System.out.println(metaData.getDatabaseProductName());
        System.out.println(metaData.getDatabaseProductVersion());

        ResultSet tables = metaData.getTables(null, null, null, null);
        DBTablePrinter.printResultSet(tables);

        PreparedStatement describeRoot = conn.prepareStatement("DESCRIBE ROOT");
        ResultSet resultSet = describeRoot.executeQuery();

        System.out.println("describe ROOT");
        DBTablePrinter.printResultSet(resultSet);

        System.out.println("describe ROOT.products");
        describeRoot = conn.prepareStatement("DESCRIBE ROOT.books");
        resultSet = describeRoot.executeQuery();

        DBTablePrinter.printResultSet(resultSet);

        System.out.println("SELECT FROM ROOT.products");
        PreparedStatement selectProducts = conn.prepareStatement("SELECT * FROM ROOT.books");
        resultSet = selectProducts.executeQuery();

        DBTablePrinter.printResultSet(resultSet);

    }
}
