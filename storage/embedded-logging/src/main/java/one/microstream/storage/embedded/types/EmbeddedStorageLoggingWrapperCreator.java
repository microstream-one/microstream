package one.microstream.storage.embedded.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import one.microstream.functional.InstanceDispatcherLogic;
import one.microstream.persistence.binary.types.BinaryStorer;
import one.microstream.persistence.binary.types.BinaryStorerCreatorLogging;
import one.microstream.persistence.types.PersistenceLegacyTypeMapper;
import one.microstream.persistence.types.PersistenceLegacyTypeMapperLogging;
import one.microstream.persistence.types.PersistenceLoader;
import one.microstream.persistence.types.PersistenceLoaderCreatorLogging;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.persistence.types.PersistenceTypeHandlerRegistry;
import one.microstream.persistence.types.PersistenceTypeHandlerRegistryLogging;
import one.microstream.persistence.types.PersistenceTypeNameMapper;
import one.microstream.persistence.types.PersistenceTypeNameMapperLogging;
import one.microstream.storage.types.StorageBackupThreadProvider;
import one.microstream.storage.types.StorageBackupThreadProviderLogging;
import one.microstream.storage.types.StorageChannelThreadProvider;
import one.microstream.storage.types.StorageChannelThreadProviderLogging;
import one.microstream.storage.types.StorageChannelsCreator;
import one.microstream.storage.types.StorageChannelsCreatorLogging;
import one.microstream.storage.types.StorageEventLogger;
import one.microstream.storage.types.StorageEventLoggerLogging;
import one.microstream.storage.types.StorageHousekeepingBroker;
import one.microstream.storage.types.StorageHousekeepingBrokerLogging;
import one.microstream.storage.types.StorageLockFileManagerThreadProvider;
import one.microstream.storage.types.StorageLockFileManagerThreadProviderLogging;
import one.microstream.storage.types.StorageSystem;
import one.microstream.storage.types.StorageSystemLogging;
import one.microstream.storage.types.StorageThreadProvider;
import one.microstream.storage.types.StorageThreadProviderLogging;

public interface EmbeddedStorageLoggingWrapperCreator extends InstanceDispatcherLogic
{
	public static EmbeddedStorageLoggingWrapperCreator New(
		final EmbeddedStorageFoundation<?> foundation
	)
	{
		return New(
			foundation,
			foundation.getInstanceDispatcherLogic()
		);
	}

	public static EmbeddedStorageLoggingWrapperCreator New(
		final EmbeddedStorageFoundation<?> foundation,
		final InstanceDispatcherLogic      delegate
	)
	{
		return new Default(
			notNull(foundation),
			mayNull(delegate)
		);
	}


	public static class Default implements EmbeddedStorageLoggingWrapperCreator
	{
		private final EmbeddedStorageFoundation<?> foundation;
		private final InstanceDispatcherLogic      delegate  ;

		Default(
			final EmbeddedStorageFoundation<?> foundation,
			final InstanceDispatcherLogic      delegate
		)
		{
			super();
			this.foundation = foundation;
			this.delegate   = delegate  ;
		}

		@Override
		public <T> T apply(final T subject)
		{
			final T result = this.applyInternal(subject);
			return this.delegate != null
				? this.delegate.apply(result)
				: result
			;
		}

		@SuppressWarnings("unchecked") // type safety ensured by logic
		private <T> T applyInternal(final T subject)
		{
			if(subject instanceof EmbeddedStorageConnectionFoundation.Default<?>)
			{
				((EmbeddedStorageConnectionFoundation<?>)subject).setInstanceDispatcher(this);
				
				return (T) EmbeddedStorageConnectionFoundationLogging.New(
					(EmbeddedStorageConnectionFoundation<?>) subject
				);
			}

			if(subject instanceof PersistenceTypeHandlerRegistry<?>
				&& !(subject instanceof PersistenceTypeHandlerManager<?>))
			{
				return (T)PersistenceTypeHandlerRegistryLogging.New(
					(PersistenceTypeHandlerRegistry<?>)subject
				);
			}
			
			if(subject instanceof PersistenceLoader.Creator<?>)
			{
				return (T)PersistenceLoaderCreatorLogging.New(
					(PersistenceLoader.Creator<?>)subject
				);
			}
			
			if(subject instanceof StorageSystem)
			{
				return (T) StorageSystemLogging.New(
					(StorageSystem) subject
				);
			}
			
			//order is important, must be setup before StorageChannelThreadProvider
			if(subject instanceof StorageThreadProvider)
			{
				return (T) StorageThreadProviderLogging.New(
					(StorageThreadProvider) subject
				);
			}
							
			//order is important, must be setup after StorageThreadProvider
			if(subject instanceof StorageChannelThreadProvider)
			{
				return (T) StorageChannelThreadProviderLogging.New(
					(StorageChannelThreadProvider) subject
				);
			}
			
			//order is important, must be setup after StorageThreadProvider
			if(subject instanceof StorageBackupThreadProvider)
			{
				return (T) StorageBackupThreadProviderLogging.New(
					(StorageBackupThreadProvider) subject
				);
			}
			
			//order is important, must be setup after StorageThreadProvider
			if(subject instanceof StorageLockFileManagerThreadProvider)
			{
				return (T) StorageLockFileManagerThreadProviderLogging.New(
					(StorageLockFileManagerThreadProvider) subject
				);
			}
			
			if(subject instanceof StorageChannelsCreator)
			{
				return (T) StorageChannelsCreatorLogging.New(
					(StorageChannelsCreator) subject
				);
			}
								
			if(subject instanceof PersistenceTypeNameMapper)
			{
				return (T) PersistenceTypeNameMapperLogging.New(
					(PersistenceTypeNameMapper) subject
				);
			}
			
			if(subject instanceof StorageHousekeepingBroker)
			{
				return (T) StorageHousekeepingBrokerLogging.New(
					(StorageHousekeepingBroker) subject
				);
			}
			
			if(subject instanceof StorageEventLogger)
			{
				return (T) StorageEventLoggerLogging.New(
					(StorageEventLogger) subject
				);
			}
			
			if(subject instanceof PersistenceLegacyTypeMapper<?>)
			{
				return (T) PersistenceLegacyTypeMapperLogging.New(
					(PersistenceLegacyTypeMapper<?>) subject
				);
			}
			
			if(subject instanceof BinaryStorer.Creator)
			{
				return (T)BinaryStorerCreatorLogging.New(
					this.foundation.getConnectionFoundation().getStorageSystem().channelCountProvider(),
					this.foundation.getConnectionFoundation().isByteOrderMismatch()
				);
			}
			
			return subject;
		}

	}

}
