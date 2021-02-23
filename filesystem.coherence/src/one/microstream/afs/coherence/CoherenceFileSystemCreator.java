package one.microstream.afs.coherence;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

import one.microstream.afs.AFileSystem;
import one.microstream.afs.blobstore.BlobStoreFileSystem;
import one.microstream.chars.XChars;
import one.microstream.configuration.exceptions.ConfigurationException;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;

public class CoherenceFileSystemCreator extends ConfigurationBasedCreator.Abstract<AFileSystem>
{
	public CoherenceFileSystemCreator()
	{
		super(AFileSystem.class);
	}
	
	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final Configuration coherenceConfiguration = configuration.child("coherence");
		if(coherenceConfiguration == null)
		{
			return null;
		}
		
		final String cacheName = coherenceConfiguration.get("cache-name");
		if(XChars.isEmpty(cacheName))
		{
			throw new ConfigurationException(coherenceConfiguration, "Coherence cache-name must be defined");
		}
		
		coherenceConfiguration.opt("cache-config").ifPresent(
			value -> System.setProperty("tangosol.coherence.cacheconfig", value)
		);
		
		final NamedCache         namedCache = CacheFactory.getCache(cacheName);
		final boolean            cache      = configuration.optBoolean("cache").orElse(true);
		final CoherenceConnector connector  = cache
			? CoherenceConnector.Caching(namedCache)
			: CoherenceConnector.New(namedCache)
		;
		return BlobStoreFileSystem.New(connector);
	}
	
}
