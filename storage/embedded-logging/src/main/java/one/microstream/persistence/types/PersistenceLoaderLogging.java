package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

public interface PersistenceLoaderLogging
	extends PersistenceLoader, PersistenceLoggingWrapper<PersistenceLoader>
{
	public static PersistenceLoaderLogging New(
		final PersistenceLoader wrapped
	)
	{
		return new Default(notNull(wrapped));
	}
	
	public class Default
		extends PersistenceLoggingWrapper.Abstract<PersistenceLoader>
		implements PersistenceLoaderLogging
		
	{
		protected Default(final PersistenceLoader wrapped)
		{
			super(wrapped);
		}
		
		@Override
		public PersistenceRoots loadRoots()
		{
			this.logger().persistenceLoader_beforeLoadRoots();
			
			final PersistenceRoots loadedRoots = this.wrapped().loadRoots();
			
			this.logger().persistenceLoader_afterLoadRoots(loadedRoots);
			
			return loadedRoots;
		}

		@Override
		public void registerSkip(final long objectId)
		{
			this.logger().persistenceLoader_beforeRegisterSkip(objectId);
			
			this.wrapped().registerSkip(objectId);
			
			this.logger().persistenceLoader_afterRegisterSkip(objectId);
		}

		@Override
		public Object get()
		{
			this.logger().persistenceLoader_beforeGet();
			
			final Object retrieved = this.wrapped().get();
			
			this.logger().persistenceLoader_afterGet(retrieved);
			
			return retrieved;
		}

		@Override
		public Object getObject(final long objectId)
		{
			this.logger().persistenceLoader_beforeGetObject(objectId);
			
			final Object retrieved = this.wrapped().getObject(objectId);
			
			this.logger().persistenceLoader_afterGetObject(objectId, retrieved);
			
			return retrieved;
		}

		@Override
		public <C extends Consumer<Object>> C collect(final C collector, final long... objectIds)
		{
			this.logger().persistenceLoader_beforeCollect(collector, objectIds);
			
			final C collected = this.wrapped().collect(collector, objectIds);
			
			this.logger().persistenceLoader_afterCollect(collected);
			
			return collected;
		}
		
	}
}
