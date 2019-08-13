package one.microstream.persistence.types;

import one.microstream.util.BufferSizeProviderIncremental;

public interface PersistenceStorer<M> extends Storer
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistenceStorer<M> initialize();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistenceStorer<M> initialize(long initialCapacity);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistenceStorer<M> reinitialize();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistenceStorer<M> reinitialize(long initialCapacity);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PersistenceStorer<M> ensureCapacity(long desiredCapacity);

	public interface Creator<M>
	{
		/**
		 * Creates a {@link PersistenceStorer} instance with a storing logic that stores instances that are
		 * encountered during the traversal of the entity graph that "require" to be stored. The actual meaning
		 * of being "required" depends on the implementation. An example for being "required" is not having an
		 * instance registered in the global object registry and associated an biunique OID.
		 * 
		 * @param objectManager
		 * @param objectRetriever
		 * @param typeManager
		 * @param target
		 * @param bufferSizeProvider
		 */
		public PersistenceStorer<M> createLazyStorer(
			PersistenceTypeHandlerManager<M> typeManager       ,
			PersistenceObjectManager         objectManager     ,
			PersistenceObjectRetriever       objectRetriever   ,
			PersistenceTarget<M>             target            ,
			BufferSizeProviderIncremental    bufferSizeProvider
		);
		
		/**
		 * Creates a storer with a default or "natural" storing logic. The default for this method
		 * (the "default default" in a way) is to delegate the call to {@link #createLazyStorer}.
		 * 
		 * @param objectManager
		 * @param objectRetriever
		 * @param typeManager
		 * @param target
		 * @param bufferSizeProvider
		 */
		public default PersistenceStorer<M> createStorer(
			final PersistenceTypeHandlerManager<M> typeManager       ,
			final PersistenceObjectManager         objectManager     ,
			final PersistenceObjectRetriever       objectRetriever   ,
			final PersistenceTarget<M>             target            ,
			final BufferSizeProviderIncremental    bufferSizeProvider
		)
		{
			return this.createLazyStorer(typeManager, objectManager, objectRetriever, target, bufferSizeProvider);
		}
		
		/**
		 * Creates a {@link PersistenceStorer} instance with a storing logic that stores every instance that is
		 * encountered during the traversal of the entity graph once.<br>
		 * Warning: This means that every (persistable) reference is traversed and every reachable instance is stored.
		 * Depending on the used data model, this can mean that the whole entity graph of an application is traversed
		 * and stored. This MIGHT be reasonable for very tiny applications, where storing simply means to start at the
		 * root entity and indiscriminately store every entity there is. Apart from this (rather academic) case,
		 * a storer with this logic should only be used for a confined entity sub-graph that has no reference "escaping"
		 * to the remaning entities.
		 * 
		 * @param objectManager
		 * @param objectRetriever
		 * @param typeManager
		 * @param target
		 * @param bufferSizeProvider
		 */
		public PersistenceStorer<M> createEagerStorer(
			PersistenceTypeHandlerManager<M> typeManager       ,
			PersistenceObjectManager         objectManager     ,
			PersistenceObjectRetriever       objectRetriever   ,
			PersistenceTarget<M>             target            ,
			BufferSizeProviderIncremental    bufferSizeProvider
		);
	}

}
