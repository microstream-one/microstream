package one.microstream.persistence.binary.types;

import one.microstream.persistence.types.PersistenceObjectManager;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.reference.ObjectSwizzling;
import one.microstream.util.BufferSizeProviderIncremental;

public class ListeningBinaryStorerCreator extends BinaryStorer.Creator.Abstract
{
	private final StorageListener listener;
		
	
	public ListeningBinaryStorerCreator(
		final BinaryChannelCountProvider channelCountProvider,
		final boolean switchByteOrder,
		final StorageListener listener
	)
	{
		super(channelCountProvider, switchByteOrder);
		this.listener = listener;
	}
	
	@Override
	public final BinaryStorer createLazyStorer(
		final PersistenceTypeHandlerManager<Binary> typeManager       ,
		final PersistenceObjectManager<Binary>      objectManager     ,
		final ObjectSwizzling                       objectRetriever   ,
		final PersistenceTarget<Binary>             target            ,
		final BufferSizeProviderIncremental         bufferSizeProvider
	)
	{
		this.validateIsStoring(target);
		
		final BinaryStorerLazyLogging storer = new BinaryStorerLazyLogging(
			objectManager         ,
			objectRetriever       ,
			typeManager           ,
			target                ,
			bufferSizeProvider    ,
			this.channelCount()   ,
			this.switchByteOrder(),
			this.listener
		);
		objectManager.registerLocalRegistry(storer);
		
		return storer;
	}
	@Override
	public BinaryStorer createEagerStorer(
		final PersistenceTypeHandlerManager<Binary> typeManager       ,
		final PersistenceObjectManager<Binary>      objectManager     ,
		final ObjectSwizzling                       objectRetriever   ,
		final PersistenceTarget<Binary>             target            ,
		final BufferSizeProviderIncremental         bufferSizeProvider
	)
	{
		this.validateIsStoring(target);
		
		final BinaryStorerEagerLogging storer = new BinaryStorerEagerLogging(
			objectManager         ,
			objectRetriever       ,
			typeManager           ,
			target                ,
			bufferSizeProvider    ,
			this.channelCount()   ,
			this.switchByteOrder(),
			this.listener
		);
		objectManager.registerLocalRegistry(storer);
		
		return storer;
	}
	
	protected void validateIsStoring(final PersistenceTarget<Binary> target)
	{
		target.validateIsStoringEnabled();
	}


	static class BinaryStorerLazyLogging extends BinaryStorer.Default
	{
		final StorageListener listener;
		
		BinaryStorerLazyLogging(
			final PersistenceObjectManager<Binary> objectManager,
			final ObjectSwizzling objectRetriever,
			final PersistenceTypeHandlerManager<Binary> typeManager,
			final PersistenceTarget<Binary> target,
			final BufferSizeProviderIncremental bufferSizeProvider,
			final int channelCount,
			final boolean switchByteOrder,
			final StorageListener listener
		)
		{
			super(objectManager, objectRetriever, typeManager, target, bufferSizeProvider, channelCount, switchByteOrder);
			this.listener = listener;
		}
		
		@Override
		public <T> void registerLazyOptional(
			final long objectId,
			final T instance,
			final PersistenceTypeHandler<Binary, T> optionalHandler
		)
		{
			super.registerLazyOptional(objectId, instance, optionalHandler);
			
			this.listener.objectStoredLazy(objectId, instance);
		}
		
	}
	
	
	static class BinaryStorerEagerLogging extends BinaryStorerLazyLogging
	{
		BinaryStorerEagerLogging(
			final PersistenceObjectManager<Binary> objectManager,
			final ObjectSwizzling objectRetriever,
			final PersistenceTypeHandlerManager<Binary> typeManager,
			final PersistenceTarget<Binary> target,
			final BufferSizeProviderIncremental bufferSizeProvider,
			final int channelCount,
			final boolean switchByteOrder,
			final StorageListener listener
		)
		{
			super(objectManager, objectRetriever, typeManager, target, bufferSizeProvider, channelCount, switchByteOrder, listener);
		}
		
		@Override
		public final <T> long apply(final T instance)
		{
			// for a "full" graph storing strategy, the logic is simply to store everything forced.
			return this.applyEager(instance);
		}
		
		@Override
		public <T> void registerLazyOptional(
			final long                              objectId       ,
			final T                                 instance       ,
			final PersistenceTypeHandler<Binary, T> optionalHandler
		)
		{
			// default is eager logic, so no-op
		}
		
		@Override
		public <T> void registerEagerOptional(
			final long                              objectId       ,
			final T                                 instance       ,
			final PersistenceTypeHandler<Binary, T> optionalHandler
		)
		{
			// default is eager logic.
			this.registerGuaranteed(objectId, instance, optionalHandler);
			
			this.listener.objectStoredEager(objectId, instance);
		}
		
	}
}
