package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import java.nio.channels.FileChannel;

public interface StorageBackupFile extends StorageNumberedFile
{
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
		public final int channelIndex()
		{
			return this.delegate.channelIndex();
		}

		@Override
		public final long number()
		{
			return this.delegate.number();
		}

		@Override
		public final StorageInventoryFile inventorize()
		{
			return this.delegate.inventorize();
		}

		@Override
		public final String qualifier()
		{
			return this.delegate.qualifier();
		}

		@Override
		public final String identifier()
		{
			return this.delegate.identifier();
		}

		@Override
		public final String name()
		{
			return this.delegate.name();
		}

		@Override
		public final long length()
		{
			return this.delegate.length();
		}

		@Override
		public final boolean delete()
		{
			return this.delegate.delete();
		}

		@Override
		public final boolean exists()
		{
			return this.delegate.exists();
		}

		@Override
		public final boolean isEmpty()
		{
			return this.delegate.isEmpty();
		}

		@Override
		public FileChannel channel()
		{
			return this.delegate.channel();
		}

		@Override
		public final boolean isOpen()
		{
			return this.delegate.isOpen();
		}

		@Override
		public final StorageFile flush()
		{
			return this.delegate.flush();
		}

		@Override
		public final void close()
		{
			this.delegate.close();
		}
		
	}
	
}
