package one.microstream.afs.oraclecloud.objectstorage.types;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import com.nimbusds.jose.util.StandardCharset;
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
		Charset             charset                 = StandardCharset.UTF_8;
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
