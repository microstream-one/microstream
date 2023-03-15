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

import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfiguration;
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfigurationBuilder;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;

import java.util.Properties;

public class ConnectionData
{

    private static final String LOCAL = "local:";

    private boolean remote;
    private final EmbeddedStorageFoundation<?> storageFoundation;

    private String configFileName;

    public ConnectionData(final String configFileName, final EmbeddedStorageFoundation<?> storageFoundation)
    {
        this.configFileName = configFileName;
        this.storageFoundation = storageFoundation;
        this.remote = false;

    }

    public boolean isRemote()
    {
        return remote;
    }

    public EmbeddedStorageFoundation getStorageFoundation()
    {
        return storageFoundation;
    }

    public String getConnectionURL()
    {
        if (isRemote())
        {
            return "FIXME";
        }
        else
        {
            return LOCAL + configFileName;
        }
    }

    public static ConnectionData forURL(final String url, final Properties info)
    {
        ConnectionData result = null;
        // url always starts with jdbc:microstream:
        String data = url.substring(MicrostreamDriver.JDBC_MICROSTREAM.length());
        if (data.startsWith(LOCAL))
        {
            // this is how we assume a local connection
            String configFileName = data.substring(LOCAL.length());
            EmbeddedStorageConfigurationBuilder configurationBuilder = EmbeddedStorageConfiguration.load(configFileName);

            result = new ConnectionData(configFileName, configurationBuilder.createEmbeddedStorageFoundation());

        }
        else
        {
            // TODO We assume that info is hostname:port
            // info must contain username and password.
            // FIXME not supported yet, so we leave result == null;
        }
        return result;
    }


}
