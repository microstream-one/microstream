
package one.microstream.persistence.binary.types;

import one.microstream.persistence.types.PersistenceLogger;
import one.microstream.persistence.types.PersistenceObjectManager;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.reference.ObjectSwizzling;
import one.microstream.util.BufferSizeProviderIncremental;


public interface BinaryStorerLogging extends BinaryStorer
{
	
	public static class Default
		extends BinaryStorer.Default
		implements BinaryStorerLogging
	{

		Default(
			final PersistenceObjectManager<Binary>      objectManager     ,
			final ObjectSwizzling                       objectRetriever   ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider,
			final int                                   channelCount      ,
			final boolean                               switchByteOrder
		)
		{
			super(objectManager, objectRetriever, typeManager, target, bufferSizeProvider, channelCount, switchByteOrder);
		}
		
		private PersistenceLogger logger()
		{
			return PersistenceLogger.get();
		}
		
		@Override
		protected long storeGraph(
			final Object root
		)
		{
			this.logger().persistenceStorer_beforeStore(this, root);
			
			final long rootId = super.storeGraph(root);

			this.logger().persistenceStorer_afterStore(this, root, rootId);
			
			return rootId;
		}

		@Override
		public Object commit()
		{
			this.logger().persistenceStorer_beforeCommit(this);

			final Object status = super.commit();

			this.logger().persistenceStorer_afterCommit(this, status);

			return status;
		}
		
		@Override
		public <T> void registerLazyOptional(
			final long                              objectId       ,
			final T                                 instance       ,
			final PersistenceTypeHandler<Binary, T> optionalHandler
		)
		{
			super.registerLazyOptional(objectId, instance, optionalHandler);
			
			this.logger().persistenceStorer_afterRegisterLazy(objectId, instance);
		}
		
	}
	
	public static class Eager
		extends BinaryStorer.Eager
		implements BinaryStorerLogging
	{
		Eager(
			final PersistenceObjectManager<Binary>      objectManager     ,
			final ObjectSwizzling                       objectRetriever   ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider,
			final int                                   channelCount      ,
			final boolean                               switchByteOrder
		)
		{
			super(objectManager, objectRetriever, typeManager, target, bufferSizeProvider, channelCount, switchByteOrder);
		}
		
		private PersistenceLogger logger()
		{
			return PersistenceLogger.get();
		}
		
		@Override
		protected long storeGraph(
			final Object root
		)
		{
			this.logger().persistenceStorer_beforeStore(this, root);
			
			final long rootId = super.storeGraph(root);

			this.logger().persistenceStorer_afterStore(this, root, rootId);
			
			return rootId;
		}

		@Override
		public Object commit()
		{
			this.logger().persistenceStorer_beforeCommit(this);

			final Object status = super.commit();

			this.logger().persistenceStorer_afterCommit(this, status);

			return status;
		}
		
		@Override
		public <T> void registerEagerOptional(
			final long                              objectId       ,
			final T                                 instance       ,
			final PersistenceTypeHandler<Binary, T> optionalHandler
		)
		{
			super.registerEagerOptional(objectId, instance, optionalHandler);
			
			this.logger().persistenceStorer_afterRegisterEager(objectId, instance);
		}
		
	}
	
}
