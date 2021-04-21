package one.microstream.persistence.binary.types;

import static one.microstream.X.notNull;

import one.microstream.persistence.types.PersistenceObjectManager;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.reference.ObjectSwizzling;
import one.microstream.util.BufferSizeProviderIncremental;

public interface BinaryStorerCreatorLogging extends BinaryStorer.Creator
{
	public static BinaryStorerCreatorLogging New(
		final BinaryChannelCountProvider channelCountProvider,
		final boolean                    switchByteOrder
	)
	{
		return new Default(
			notNull(channelCountProvider),
			switchByteOrder
		);
	}
	
	
	public static class Default
		extends BinaryStorer.Creator.Abstract
		implements BinaryStorerCreatorLogging
	{
		Default(
			final BinaryChannelCountProvider channelCountProvider,
			final boolean                    switchByteOrder
		)
		{
			super(channelCountProvider, switchByteOrder);
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
			
			final BinaryStorerLogging.Default storer = new BinaryStorerLogging.Default(
				objectManager         ,
				objectRetriever       ,
				typeManager           ,
				target                ,
				bufferSizeProvider    ,
				this.channelCount()   ,
				this.switchByteOrder()
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
			
			final BinaryStorerLogging.Eager storer = new BinaryStorerLogging.Eager(
				objectManager         ,
				objectRetriever       ,
				typeManager           ,
				target                ,
				bufferSizeProvider    ,
				this.channelCount()   ,
				this.switchByteOrder()
			);
			objectManager.registerLocalRegistry(storer);
			
			return storer;
		}
		
		protected void validateIsStoring(final PersistenceTarget<Binary> target)
		{
			target.validateIsStoringEnabled();
		}
				
	}
	
}
