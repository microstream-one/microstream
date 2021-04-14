package one.microstream.cache.hibernate.types;

import java.util.Collections;

import org.hibernate.boot.registry.selector.SimpleStrategyRegistrationImpl;
import org.hibernate.boot.registry.selector.StrategyRegistration;
import org.hibernate.boot.registry.selector.StrategyRegistrationProvider;
import org.hibernate.cache.spi.RegionFactory;

public class CacheStrategyRegistrationProvider implements StrategyRegistrationProvider
{
	public CacheStrategyRegistrationProvider()
	{
		super();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterable<StrategyRegistration> getStrategyRegistrations()
	{
		SimpleStrategyRegistrationImpl<RegionFactory> registration = new SimpleStrategyRegistrationImpl<>(
			RegionFactory.class,
			CacheRegionFactory.class,
			"jcache",
			CacheRegionFactory.class.getName(),
			CacheRegionFactory.class.getSimpleName()
		);
		return Collections.singleton(registration);
	}	
}
