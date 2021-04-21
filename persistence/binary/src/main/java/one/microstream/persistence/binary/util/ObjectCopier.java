package one.microstream.persistence.binary.util;

import static one.microstream.X.notNull;

import java.io.Closeable;

import one.microstream.X;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.binary.types.BinaryPersistenceFoundation;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceContextDispatcher;
import one.microstream.persistence.types.PersistenceIdSet;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceSource;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.persistence.types.PersistenceTypeDictionaryManager;
import one.microstream.reference.Reference;

public interface ObjectCopier extends Closeable
{
	public <T> T copy(T source);
	
	@Override
	public void close();
	
	
	public static ObjectCopier New()
	{
		return new Default(BinaryPersistence.Foundation());
	}
	
	public static ObjectCopier New(final BinaryPersistenceFoundation<?> foundation)
	{
		return new Default(
			notNull(foundation)
		);
	}
	
	
	public static class Default implements ObjectCopier
	{
		private final BinaryPersistenceFoundation<?> foundation        ;
		private PersistenceManager<Binary>           persistenceManager;
				
		Default(final BinaryPersistenceFoundation<?> foundation)
		{
			super();
			this.foundation = foundation;
		}

		@SuppressWarnings("unchecked")
		@Override
		public synchronized <T> T copy(final T source)
		{
			this.lazyInit();
			
			this.persistenceManager.store(source);
			return (T)this.persistenceManager.get();
		}
		
		@Override
		public synchronized void close()
		{
			if(this.persistenceManager != null)
			{
				this.persistenceManager.objectRegistry().clearAll();
				this.persistenceManager.close();
				this.persistenceManager = null;
			}
		}
		
		private void lazyInit()
		{
			if(this.persistenceManager == null)
			{
				final Reference<Binary> buffer = X.Reference(null);
				final CopySource        source = ()   -> X.Constant(buffer.get());
				final CopyTarget        target = data -> buffer.set(data);
				
				final BinaryPersistenceFoundation<?> foundation = this.foundation
					.setPersistenceSource(source)
					.setPersistenceTarget(target)
					.setContextDispatcher(PersistenceContextDispatcher.LocalObjectRegistration())
				;
				
				foundation.setTypeDictionaryManager(
					PersistenceTypeDictionaryManager.Transient(
						foundation.getTypeDictionaryCreator()
					)
				);
				
				this.persistenceManager = foundation.createPersistenceManager();
			}
			else
			{
				this.persistenceManager.objectRegistry().truncateAll();
			}
		}
		
		
		static interface CopySource extends PersistenceSource<Binary>
		{
			@Override
			default XGettingCollection<? extends Binary> readByObjectIds(final PersistenceIdSet[] oids)
				throws PersistenceExceptionTransfer
			{
				return null;
			}
		}
		
		
		static interface CopyTarget extends PersistenceTarget<Binary>
		{
			@Override
			default boolean isWritable()
			{
				return true;
			}
		}
		
	}
	
}
