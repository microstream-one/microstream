package net.jadoth.com;

import static net.jadoth.X.notNull;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.persistence.types.PersistenceIdStrategy;

@FunctionalInterface
public interface ComPersistenceAdaptorCreator<C>
{
	/* (11.02.2019 TM)FIXME: JET-49: specifying the host byteOrder is missing here.
	 * Or maybe that is done via the protocol and the protocol provider (creator).
	 */
	
	public ComPersistenceAdaptor<C> createPersistenceAdaptor(
		PersistenceIdStrategy  hostIdStrategyInitialization,
		XGettingEnum<Class<?>> entityTypes                 ,
		PersistenceIdStrategy  hostIdStrategy
	);
	
	public default ComPersistenceAdaptor<C> createHostPersistenceAdaptor(
		final PersistenceIdStrategy  hostIdStrategyInitialization,
		final XGettingEnum<Class<?>> entityTypes                 ,
		final PersistenceIdStrategy  hostIdStrategy
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
