package one.microstream.afs.oraclecloud.objectstorage.types;

/*-
 * #%L
 * microstream-afs-oraclecloud-objectstorage
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.oracle.bmc.ClientConfiguration;
import com.oracle.bmc.ClientConfiguration.ClientConfigurationBuilder;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;

import one.microstream.afs.blobstore.types.BlobStoreFileSystem;
import one.microstream.afs.types.AFileSystem;
import one.microstream.chars.XChars;
import one.microstream.configuration.exceptions.ConfigurationException;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;

public class OracleCloudObjectStorageFileSystemCreator extends ConfigurationBasedCreator.Abstract<AFileSystem>
{
	private final static String CLASSPATH_PREFIX = "classpath:";

	public OracleCloudObjectStorageFileSystemCreator()
	{
		super(AFileSystem.class);
	}

	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final Configuration objectStorageConfiguration = configuration.child("oraclecloud.object-storage");
		if(objectStorageConfiguration == null)
		{
			return null;
		}

		String              filePath                = null;
		String              profile                 = null;
		Charset             charset                 = StandardCharsets.UTF_8;
		final Configuration configFileConfiguration = objectStorageConfiguration.child("config-file");
		if(configFileConfiguration != null)
		{
			filePath = configFileConfiguration.get("path");
			profile  = configFileConfiguration.get("profile");

			final String charsetName = configFileConfiguration.get("charset");
			if(charsetName != null)
			{
				charset = Charset.forName(charsetName);
			}
		}

		try
		{
			final ConfigFileReader.ConfigFile configFile = XChars.isEmpty(filePath)
				? ConfigFileReader.parseDefault(profile)
				: ConfigFileReader.parse(
					this.configFileInputStream(configFileConfiguration, filePath),
					profile,
					charset
				)
			;
			final AuthenticationDetailsProvider authDetailsProvider =
				new ConfigFileAuthenticationDetailsProvider(configFile)
			;

			final ClientConfigurationBuilder clientConfigurationBuilder = ClientConfiguration.builder();
			final Configuration              clientConfiguration        = objectStorageConfiguration.child("client");
			if(clientConfiguration != null)
			{
				this.createClientConfiguration(
					clientConfigurationBuilder,
					clientConfiguration
				);
			}

			final ObjectStorageClient client = new ObjectStorageClient(
				authDetailsProvider,
				clientConfigurationBuilder.build()
			);
			objectStorageConfiguration.opt("region").ifPresent(
				value -> client.setRegion(value)
			);
			objectStorageConfiguration.opt("endpoint").ifPresent(
				value -> client.setEndpoint(value)
			);

			final boolean        cache           = configuration.optBoolean("cache").orElse(true);
			final OracleCloudObjectStorageConnector connector       = cache
				? OracleCloudObjectStorageConnector.Caching(client)
				: OracleCloudObjectStorageConnector.New(client)
			;
			return BlobStoreFileSystem.New(connector);
		}
		catch(final IOException e)
		{
			throw new ConfigurationException(objectStorageConfiguration, e);
		}
	}

	private InputStream configFileInputStream(
		final Configuration configuration,
		final String        path
	)
	throws IOException
	{
		if(path.toLowerCase().startsWith(CLASSPATH_PREFIX))
		{
			return this.getClass().getResourceAsStream(path.substring(CLASSPATH_PREFIX.length()));
		}

		try
		{
			final URL url = new URL(path);
			return url.openStream();
		}
		catch(final MalformedURLException e)
		{
			return new FileInputStream(path);
		}
	}

	private void createClientConfiguration(
		final ClientConfigurationBuilder builder      ,
		final Configuration              configuration
	)
	{
		configuration.optInteger("connection-timeout-millis").ifPresent(
			value -> builder.connectionTimeoutMillis(value)
		);

		configuration.optInteger("read-timeout-millis").ifPresent(
			value -> builder.readTimeoutMillis(value)
		);

		configuration.optInteger("max-async-threads").ifPresent(
			value -> builder.maxAsyncThreads(value)
		);
	}

}
