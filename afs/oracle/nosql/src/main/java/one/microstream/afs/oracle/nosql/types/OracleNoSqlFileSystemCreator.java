package one.microstream.afs.oracle.nosql.types;

import java.time.Duration;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import one.microstream.afs.blobstore.types.BlobStoreFileSystem;
import one.microstream.afs.types.AFileSystem;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;
import oracle.kv.Consistency;
import oracle.kv.Durability;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.PasswordCredentials;

public class OracleNoSqlFileSystemCreator extends ConfigurationBasedCreator.Abstract<AFileSystem>
{
	public OracleNoSqlFileSystemCreator()
	{
		super(AFileSystem.class);
	}

	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final Configuration nosqlConfiguration = configuration.child("oracle.nosql");
		if(nosqlConfiguration == null)
		{
			return null;
		}

		final KVStoreConfig kvStoreConfig = this.createKVStoreConfig(nosqlConfiguration);

		final String username = nosqlConfiguration.get("username");
		final String password = nosqlConfiguration.get("password");

		final KVStore kvStore = username != null && password != null
			? KVStoreFactory.getStore(
				kvStoreConfig,
				new PasswordCredentials(username, password.toCharArray()),
				null
			)
			: KVStoreFactory.getStore(kvStoreConfig)
		;

		final boolean              cache     = configuration.optBoolean("cache").orElse(true);
		final OracleNoSqlConnector connector = cache
			? OracleNoSqlConnector.Caching(kvStore)
			: OracleNoSqlConnector.New(kvStore)
		;
		return BlobStoreFileSystem.New(connector);
	}

	private KVStoreConfig createKVStoreConfig(
		final Configuration configuration
	)
	{
		final KVStoreConfig storeConfig = new KVStoreConfig(
			configuration.get("store-name"),
			configuration.get("helper-hosts").split(",")
		);

		configuration.opt("check-interval", Duration.class).ifPresent(
			value -> storeConfig.setCheckInterval(value.toMillis(), TimeUnit.MILLISECONDS)
		);

		this.optConsistency(configuration, "consistency").ifPresent(
			value -> storeConfig.setConsistency(value)
		);

		this.optDurability(configuration, "durability").ifPresent(
			value -> storeConfig.setDurability(value)
		);

		configuration.optInteger("lob-chunk-size").ifPresent(
			value -> storeConfig.setLOBChunkSize(value)
		);

		configuration.optInteger("lob-chunks-per-partition").ifPresent(
			value -> storeConfig.setLOBChunksPerPartition(value)
		);

		configuration.opt("lob-timeout", Duration.class).ifPresent(
			value -> storeConfig.setLOBTimeout(value.toMillis(), TimeUnit.MILLISECONDS)
		);

		configuration.optLong("lob-verification-bytes").ifPresent(
			value -> storeConfig.setLOBVerificationBytes(value)
		);

		configuration.optInteger("max-check-retries").ifPresent(
			value -> storeConfig.setMaxCheckRetries(value)
		);

		configuration.opt("network-roundtrip-timeout", Duration.class).ifPresent(
			value -> storeConfig.setNetworkRoundtripTimeout(value.toMillis(), TimeUnit.MILLISECONDS)
		);

		configuration.opt("read-zones").ifPresent(
			value -> storeConfig.setReadZones(value.split(","))
		);

		configuration.opt("registry-open-timeout", Duration.class).ifPresent(
			value -> storeConfig.setRegistryOpenTimeout(value.toMillis(), TimeUnit.MILLISECONDS)
		);

		configuration.opt("registry-read-timeout", Duration.class).ifPresent(
			value -> storeConfig.setRegistryReadTimeout(value.toMillis(), TimeUnit.MILLISECONDS)
		);

		configuration.opt("request-timeout", Duration.class).ifPresent(
			value -> storeConfig.setRequestTimeout(value.toMillis(), TimeUnit.MILLISECONDS)
		);

		configuration.optInteger("sg-attrs-cache-timeout").ifPresent(
			value -> storeConfig.setSGAttrsCacheTimeout(value)
		);

		configuration.opt("socket-open-timeout", Duration.class).ifPresent(
			value -> storeConfig.setSocketOpenTimeout(value.toMillis(), TimeUnit.MILLISECONDS)
		);

		configuration.opt("socket-read-timeout", Duration.class).ifPresent(
			value -> storeConfig.setSocketReadTimeout(value.toMillis(), TimeUnit.MILLISECONDS)
		);

		configuration.optBoolean("use-async").ifPresent(
			value -> storeConfig.setUseAsync(value)
		);

		final Configuration securityPropertiesConfiguration = configuration.child("security-properties");
		if(securityPropertiesConfiguration != null)
		{
			final Properties securityProperties = new Properties();
			securityProperties.putAll(securityPropertiesConfiguration.coalescedMap());
			storeConfig.setSecurityProperties(securityProperties);
		}

		return storeConfig;
	}

	private Optional<Consistency> optConsistency(
		final Configuration nosqlConfiguration,
		final String        key
	)
	{
		return nosqlConfiguration.opt(key).map(name ->
		{
			switch(name.toUpperCase())
			{
				case "NONE_REQUIRED": return Consistency.NONE_REQUIRED;
				case "ABSOLUTE"     : return Consistency.ABSOLUTE     ;
				default             : return null                     ;
			}
		});
	}

	private Optional<Durability> optDurability(
		final Configuration nosqlConfiguration,
		final String        key
	)
	{
		return nosqlConfiguration.opt(key).map(name ->
		{
			switch(name.toUpperCase())
			{
				case "COMMIT_SYNC"         : return Durability.COMMIT_SYNC         ;
				case "COMMIT_NO_SYNC"      : return Durability.COMMIT_NO_SYNC      ;
				case "COMMIT_WRITE_NO_SYNC": return Durability.COMMIT_WRITE_NO_SYNC;
				default                    : return null                           ;
			}
		});
	}

}
