package net.jadoth.com;

import static net.jadoth.X.notNull;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.swizzling.types.SwizzleIdStrategy;

@FunctionalInterface
public interface ComPersistenceAdaptorCreator<C>
{
	public ComPersistenceAdaptor<C> createPersistenceAdaptor(
		SwizzleIdStrategy      hostIdStrategyInitialization,
		XGettingEnum<Class<?>> entityTypes                 ,
		SwizzleIdStrategy      hostIdStrategy
	);
	
	public default ComPersistenceAdaptor<C> createHostPersistenceAdaptor(
		final SwizzleIdStrategy      hostIdStrategyInitialization,
		final XGettingEnum<Class<?>> entityTypes                 ,
		final SwizzleIdStrategy      hostIdStrategy
	)
	{
		return this.createPersistenceAdaptor(
			notNull(hostIdStrategyInitialization),
			notNull(entityTypes)                 ,
			notNull(hostIdStrategy)
		);
	}
	
	public default ComPersistenceAdaptor<C> createClientPersistenceAdaptor()
	{
		return this.createPersistenceAdaptor(null, null, null);
	}
	
}
