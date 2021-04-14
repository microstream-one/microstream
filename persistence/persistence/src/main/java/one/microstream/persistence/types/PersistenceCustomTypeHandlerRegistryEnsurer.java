package one.microstream.persistence.types;

import one.microstream.reference.Reference;

/**
 * 
 * 
 *
 * @param <D>
 */
/* (16.10.2019 TM)NOTE:
 * Required to replace/modularize the calling of BinaryPersistence#createDefaultCustomTypeHandlerRegistry
 */
@FunctionalInterface
public interface PersistenceCustomTypeHandlerRegistryEnsurer<D>
{
	public PersistenceCustomTypeHandlerRegistry<D> ensureCustomTypeHandlerRegistry(
		PersistenceFoundation<D, ? extends PersistenceFoundation<D, ?>> foundation,
		Reference<PersistenceTypeHandlerManager<D>>    referenceTypeHandlerManager
	);
}
