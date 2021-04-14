package one.microstream.afs.redis.types;

import one.microstream.afs.blobstore.types.BlobStoreFileSystem;
import one.microstream.afs.types.AFileSystem;
import one.microstream.chars.XChars;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;

public class RedisFileSystemCreator extends ConfigurationBasedCreator.Abstract<AFileSystem>
{
	public RedisFileSystemCreator()
	{
		super(AFileSystem.class);
	}

	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final String redisUri = configuration.get("redis.uri");
		if(XChars.isEmpty(redisUri))
		{
			return null;
		}

		final boolean        cache     = configuration.optBoolean("cache").orElse(true);
		final RedisConnector connector = cache
			? RedisConnector.Caching(redisUri)
			: RedisConnector.New(redisUri)
		;
		return BlobStoreFileSystem.New(connector);
	}

}
