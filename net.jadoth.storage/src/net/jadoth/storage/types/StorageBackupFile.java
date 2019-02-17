package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

public interface StorageBackupFile extends StorageNumberedFile
{
	// (16.02.2019 TM)FIXME: JET-55: StorageBackupFile
	
	public static StorageBackupFile New(final StorageNumberedFile file)
	{
		return new StorageBackupFile.Implementation(
			notNull(file)
		);
	}
	
	public final class Implementation implements StorageBackupFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final StorageNumberedFile delegate;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final StorageNumberedFile delegate)
		{
			super();
			this.delegate = delegate;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public int channelIndex()
		{
			return this.delegate.channelIndex();
		}

		@Override
		public long number()
		{
			return this.delegate.number();
		}

		@Override
		public StorageInventoryFile inventorize()
		{
			return this.delegate.inventorize();
		}

		@Override
		public String qualifier()
		{
			return this.delegate.qualifier();
		}

		@Override
		public String identifier()
		{
			return this.delegate.identifier();
		}

		@Override
		public String name()
		{
			return this.delegate.name();
		}

		@Override
		public long length()
		{
			return this.delegate.length();
		}

		@Override
		public boolean delete()
		{
			return this.delegate.delete();
		}

		@Override
		public boolean exists()
		{
			return this.delegate.exists();
		}
		
	}
}
