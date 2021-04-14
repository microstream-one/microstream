package one.microstream.afs.oracle.coherence.types;

import java.util.Map;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

import one.microstream.afs.blobstore.types.BlobStoreFileSystem;
import one.microstream.afs.types.AFileSystem;
import one.microstream.chars.XChars;
import one.microstream.configuration.exceptions.ConfigurationException;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationBasedCreator;

public class OracleCoherenceFileSystemCreator extends ConfigurationBasedCreator.Abstract<AFileSystem>
{
	public OracleCoherenceFileSystemCreator()
	{
		super(AFileSystem.class);
	}
	
	@Override
	public AFileSystem create(
		final Configuration configuration
	)
	{
		final Configuration coherenceConfiguration = configuration.child("oracle.coherence");
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
		
		final NamedCache<String, Map<String, Object>> namedCache = CacheFactory.getCache(cacheName);
		final boolean                                 useCache   = configuration.optBoolean("cache").orElse(true);
		final OracleCoherenceConnector connector  = useCache
			? OracleCoherenceConnector.Caching(namedCache)
			: OracleCoherenceConnector.New(namedCache)
		;
		return BlobStoreFileSystem.New(connector);
	}
	
}
