package one.microstream.persistence.types;

import one.microstream.reference.Reference;

/**
 * 
 * @author TM
 *
 * @param <M>
 */
/* (16.10.2019 TM)NOTE:
 * Required to replace/modularize the calling of BinaryPersistence#createDefaultCustomTypeHandlerRegistry
 */
@FunctionalInterface
public interface PersistenceCustomTypeHandlerRegistryEnsurer<M>
{
	public PersistenceCustomTypeHandlerRegistry<M> ensureCustomTypeHandlerRegistry(
		PersistenceFoundation<M, ? extends PersistenceFoundation<M, ?>> foundation,
		Reference<PersistenceTypeHandlerManager<M>> referenceTypeHandlerManager
	);
}
