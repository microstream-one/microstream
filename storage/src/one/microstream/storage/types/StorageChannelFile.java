package one.microstream.storage.types;

import one.microstream.afs.AFile;

public interface StorageChannelFile extends StorageFile, StorageHashChannelPart
{
	@Override
	public int channelIndex();
	
	
	
	public abstract class Abstract extends StorageFile.Abstract implements StorageChannelFile
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
				
	}
	
}
