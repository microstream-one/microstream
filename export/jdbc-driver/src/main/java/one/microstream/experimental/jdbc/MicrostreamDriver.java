package one.microstream.experimental.jdbc;

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


import one.microstream.experimental.jdbc.exception.StorageNotFoundException;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Implementation of {@code Driver} for MicroStream.
 */
public class MicrostreamDriver implements Driver
{

    private static final Logger PARENT_LOGGER = Logger.getLogger("one.microstream.experimental.jdbc");
    private static final Driver INSTANCE = new MicrostreamDriver();
    public static final String JDBC_MICROSTREAM = "jdbc:microstream:";
    public static final int MICROSTREAM_JDBC_MAJOR = 8;
    public static final int MICROSTREAM_JDBC_MINOR = 0;
    private static boolean registered;

    // Auto registration.
    static
    {
        load();
    }

    public static synchronized Driver load()
    {
        try
        {
            if (!registered)
            {
                registered = true;
                DriverManager.registerDriver(INSTANCE);
            }
        } catch (SQLException e)
        {
            // FIXME
            throw new RuntimeException(e);
        }

        return INSTANCE;
    }

    @Override
    public Connection connect(final String url, final Properties info) throws SQLException
    {
        ConnectionData connectionData = ConnectionData.forURL(url, info);
        if (connectionData == null)
        {
            throw new StorageNotFoundException(url);
        }
        return new MicrostreamConnection(connectionData);
    }

    @Override
    public boolean acceptsURL(final String url) throws SQLException
    {
        return url.startsWith(JDBC_MICROSTREAM);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) throws SQLException
    {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion()
    {
        return MICROSTREAM_JDBC_MAJOR;
    }

    @Override
    public int getMinorVersion()
    {
        return MICROSTREAM_JDBC_MINOR;
    }

    @Override
    public boolean jdbcCompliant()
    {
        // We are not compliant!!
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        return PARENT_LOGGER;
    }
}
