package one.microstream.afs.redis;

import one.microstream.afs.AFileSystem;
import one.microstream.afs.blobstore.BlobStoreFileSystem;
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
		final String redisUrl = configuration.get("redis-url");
		if(XChars.isEmpty(redisUrl))
		{
			return null;
		}
		
		final boolean        cache     = configuration.optBoolean("cache").orElse(true);
		final RedisConnector connector = cache
			? RedisConnector.Caching(redisUrl)
			: RedisConnector.New(redisUrl)
		;
		return BlobStoreFileSystem.New(connector);
	}
	
}
