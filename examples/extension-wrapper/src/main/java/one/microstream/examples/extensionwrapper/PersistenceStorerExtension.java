
package one.microstream.examples.extensionwrapper;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectManager;
import one.microstream.persistence.types.PersistenceStorer;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.reference.ObjectSwizzling;
import one.microstream.util.BufferSizeProviderIncremental;


/**
 * Extension for {@link PersistenceStorer} which adds logic to store operations
 *
 */
public class PersistenceStorerExtension extends PersistenceStorerWrapper
{
	public PersistenceStorerExtension(final PersistenceStorer delegate)
	{
		super(delegate);
	}
	
	private void beforeStoreObject(final Object instance)
	{
		System.out.println("Storing " + instance.getClass().getName() + "@" + System.identityHashCode(instance));
	}
	
	@Override
	public long store(final Object instance)
	{
		this.beforeStoreObject(instance);
		
		return super.store(instance);
	}
	
	@Override
	public void storeAll(final Iterable<?> instances)
	{
		instances.forEach(this::beforeStoreObject);
		
		super.storeAll(instances);
	}
	
	@Override
	public long[] storeAll(final Object... instances)
	{
		for(final Object instance : instances)
		{
			this.beforeStoreObject(instance);
		}
		
		return super.storeAll(instances);
	}
	
	
	
	
	public static class Creator implements PersistenceStorer.Creator<Binary>
	{
		private final PersistenceStorer.Creator<Binary> delegate;

		public Creator(PersistenceStorer.Creator<Binary> delegate)
		{
			super();
			this.delegate = delegate;
		}

		@Override
		public PersistenceStorer createLazyStorer(
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceObjectManager<Binary>      objectManager     ,
			final ObjectSwizzling                       objectRetriever   ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider
		)
		{
			return new PersistenceStorerExtension(
				this.delegate.createLazyStorer(typeManager, objectManager, objectRetriever, target, bufferSizeProvider)
			);
		}

		@Override
		public PersistenceStorer createEagerStorer(
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceObjectManager<Binary>      objectManager     ,
			final ObjectSwizzling                       objectRetriever   ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider
		)
		{
			return new PersistenceStorerExtension(
				this.delegate.createEagerStorer(typeManager, objectManager, objectRetriever, target, bufferSizeProvider)
			);
		}
		
		
	}
	
}
