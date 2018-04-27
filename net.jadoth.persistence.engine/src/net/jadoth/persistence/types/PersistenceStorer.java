package net.jadoth.persistence.types;

import net.jadoth.swizzling.types.SwizzleObjectManager;
import net.jadoth.swizzling.types.SwizzleObjectSupplier;



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
		public PersistenceStorer<M> createStorer(
			SwizzleObjectManager             objectManager     ,
			SwizzleObjectSupplier            objectSupplier    ,
			PersistenceTypeHandlerManager<M> typeManager       ,
			PersistenceTarget<M>             target            ,
			BufferSizeProvider               bufferSizeProvider
		);
	}

}
