package one.microstream.storage.types;

import one.microstream.afs.AFile;

public interface StorageDataFile extends StorageChannelFile, StorageClosableFile
{
	@Override
	public int channelIndex();
	
	public long number();
	
	
	
	@FunctionalInterface
	public interface Creator<F extends StorageDataFile>
	{
		public F createDataFile(AFile file, int channelIndex, long number);
	}
	
	
	
	public abstract class Abstract extends StorageChannelFile.Abstract implements StorageDataFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long number;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(final AFile file, final int channelIndex, final long number)
		{
			super(file, channelIndex);
			this.number = number;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final long number()
		{
			return this.number;
		}
		
		@Override
		public synchronized boolean isOpen()
		{
			return this.internalIsOpen();
		}
		
		@Override
		public synchronized boolean close()
		{
			return this.internalClose();
		}
		
	}
	
}
