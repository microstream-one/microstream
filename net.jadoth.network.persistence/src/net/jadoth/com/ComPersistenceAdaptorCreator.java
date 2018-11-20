package net.jadoth.com;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.swizzling.types.SwizzleIdStrategy;

public interface ComPersistenceAdaptorCreator<C>
{
	public ComPersistenceAdaptor<C> createPersistenceAdaptor(
		SwizzleIdStrategy      hostIdStrategyInitialization,
		XGettingEnum<Class<?>> entityTypes                 ,
		SwizzleIdStrategy      hostIdStrategy
	);
}
