package one.microstream.storage.types;

public interface StorageBackupHandlerLogging extends StorageBackupHandler, StorageLoggingWrapper<StorageBackupHandler>
{
	static StorageBackupHandler New(final StorageBackupHandler wrapped)
	{
		return new Default(wrapped);
	}

	public class Default
		extends StorageLoggingWrapper.Abstract<StorageBackupHandler>
		implements StorageBackupHandlerLogging
	{
		public Default(final StorageBackupHandler wrapped)
		{
			super(wrapped);
		}
	
		@Override
		public StorageBackupSetup setup()
		{
			return this.wrapped().setup();
		}

		@Override
		public void initialize(final int channelIndex)
		{
			this.logger().storageBackupHandler_beforeInitialize(channelIndex);
			
			this.wrapped().initialize(channelIndex);
			
			this.logger().storageBackupHandler_afterInitialize(channelIndex);
		}

		@Override
		public void synchronize(final StorageInventory storageInventory)
		{
			this.logger().storageBackupHandler_beforeSynchronize(storageInventory);
			
			this.wrapped().synchronize(storageInventory);
			
			this.logger().storageBackupHandler_afterSynchronize(storageInventory);
		}

		@Override
		public void copyFilePart(final StorageLiveChannelFile<?> sourceFile, final long sourcePosition, final long length)
		{
			this.logger().storageBackupHandler_beforeCopyFilePart(sourceFile, sourcePosition, length);
			
			this.wrapped().copyFilePart(sourceFile, sourcePosition, length);
			
			this.logger().storageBackupHandler_afterCopyFilePart(sourceFile, sourcePosition, length);
		}

		@Override
		public void truncateFile(final StorageLiveChannelFile<?> file, final long newLength)
		{
			this.logger().storageBackupHandler_beforeTruncateFile(file, newLength);
			
			this.wrapped().truncateFile(file, newLength);
			
			this.logger().storageBackupHandler_afterTruncateFile(file, newLength);
			
		}

		@Override
		public void deleteFile(final StorageLiveChannelFile<?> file)
		{
			this.logger().storageBackupHandler_beforeDeleteFile(file);
			
			this.wrapped().deleteFile(file);
			
			this.logger().storageBackupHandler_afterDeleteFile(file);
		}

		@Override
		public StorageBackupHandler start()
		{
			this.logger().storageBackupHandler_beforeStart();
			
			final StorageBackupHandler storageBackupHandler = this.wrapped().start();
			
			this.logger().storageBackupHandler_afterStart(storageBackupHandler);
			
			return storageBackupHandler;
		}

		@Override
		public StorageBackupHandler stop()
		{
			this.logger().storageBackupHandler_beforeStop();
			
			final StorageBackupHandler storageBackupHandler = this.wrapped().stop();
			
			this.logger().storageBackupHandler_afterStop(storageBackupHandler);
			
			return storageBackupHandler;
		}

		@Override
		public boolean isRunning()
		{
			return this.wrapped().isRunning();
		}

		@Override
		public boolean isActive()
		{
			return this.wrapped().isActive();
		}

		@Override
		public StorageBackupHandler setRunning(final boolean running)
		{
			this.logger().storageBackupHandler_beforeSetRunning(running);
			
			final StorageBackupHandler storageBackupHandler = this.wrapped().setRunning(running);
			
			this.logger().storageBackupHandler_afterSetRunning(storageBackupHandler);
			
			return storageBackupHandler;
		}

		@Override
		public void run()
		{
			this.logger().storageBackupHandler_beforeRun();
			
			this.wrapped().run();
			
			this.logger().storageBackupHandler_afterRun();
		}
		
	}
	
}
