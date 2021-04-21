package one.microstream.storage.types;

public interface StorageDataFileEvaluatorLogging extends StorageDataFileEvaluator, StorageLoggingWrapper<StorageDataFileEvaluator>
{
	public static StorageDataFileEvaluatorLogging New(
		final StorageDataFileEvaluator wrapped
	)
	{
		return new Default(wrapped);
	}
	
	public static class Default	extends StorageLoggingWrapper.Abstract<StorageDataFileEvaluator> implements StorageDataFileEvaluatorLogging
	{
		protected Default(
			final StorageDataFileEvaluator wrapped
		)
		{
			super(wrapped);
		}

		@Override
		public boolean needsDissolving(final StorageLiveDataFile storageFile)
		{
			this.logger().storageDataFileEvaluator_beforeNeedsDissolving(storageFile);
			
			final boolean needsDissolving = this.wrapped().needsDissolving(storageFile);
			
			this.logger().storageDataFileEvaluator_afterNeedsDissolving(storageFile, needsDissolving);
			
			return needsDissolving;
		}

		@Override
		public boolean needsRetirement(final long fileTotalLength)
		{
			return this.wrapped().needsRetirement(fileTotalLength);
		}

		@Override
		public int fileMinimumSize()
		{
			return this.wrapped().fileMinimumSize();
		}

		@Override
		public int fileMaximumSize()
		{
			return this.wrapped().fileMaximumSize();
		}
	}


}
