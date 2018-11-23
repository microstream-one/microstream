package net.jadoth.com;

import static net.jadoth.X.notNull;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.persistence.types.PersistenceIdStrategy;

@FunctionalInterface
public interface ComPersistenceAdaptorCreator<C>
{
	public ComPersistenceAdaptor<C> createPersistenceAdaptor(
		PersistenceIdStrategy      hostIdStrategyInitialization,
		XGettingEnum<Class<?>> entityTypes                 ,
		PersistenceIdStrategy      hostIdStrategy
	);
	
	public default ComPersistenceAdaptor<C> createHostPersistenceAdaptor(
		final PersistenceIdStrategy      hostIdStrategyInitialization,
		final XGettingEnum<Class<?>> entityTypes                 ,
		final PersistenceIdStrategy      hostIdStrategy
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
