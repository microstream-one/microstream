package one.microstream.storage.types;

import one.microstream.afs.types.AFile;

public interface StorageChannelFile extends StorageFile, StorageHashChannelPart
{
	@Override
	public int channelIndex();
	
	
	
	
	public abstract class Abstract
	extends StorageFile.Abstract
	implements StorageChannelFile, StorageClosableFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int channelIndex;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(final AFile file, final int channelIndex)
		{
			super(file);
			this.channelIndex = channelIndex;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final int channelIndex()
		{
			return this.channelIndex;
		}
		
//		@Override
//		public synchronized boolean isOpen()
//		{
//			return this.internalIsOpen();
//		}
//
//		@Override
//		public synchronized boolean close()
//		{
//			return this.internalClose();
//		}
				
	}
	
}
