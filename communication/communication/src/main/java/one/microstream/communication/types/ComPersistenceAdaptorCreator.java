package one.microstream.communication.types;

import static one.microstream.X.notNull;

import java.nio.ByteOrder;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.types.PersistenceIdStrategy;

@FunctionalInterface
public interface ComPersistenceAdaptorCreator<C>
{
	public ComPersistenceAdaptor<C> createPersistenceAdaptor(
		PersistenceIdStrategy  hostIdStrategyInitialization,
		XGettingEnum<Class<?>> entityTypes                 ,
		ByteOrder              hostByteOrder               ,
		PersistenceIdStrategy  hostIdStrategy
	);
	
	public default ComPersistenceAdaptor<C> createHostPersistenceAdaptor(
		final PersistenceIdStrategy  hostIdStrategyInitialization,
		final XGettingEnum<Class<?>> entityTypes                 ,
		final ByteOrder              hostByteOrder               ,
		final PersistenceIdStrategy  hostIdStrategy
	)
	{
		return this.createPersistenceAdaptor(
			notNull(hostIdStrategyInitialization),
			notNull(entityTypes)                 ,
			notNull(hostByteOrder),
			notNull(hostIdStrategy)
		);
	}
	
	public default ComPersistenceAdaptor<C> createClientPersistenceAdaptor()
	{
		return this.createPersistenceAdaptor(null, null, null, null);
	}
	
}
