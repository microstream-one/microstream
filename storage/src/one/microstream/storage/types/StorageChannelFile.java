package one.microstream.storage.types;

import one.microstream.afs.AFile;
import one.microstream.afs.AWritableFile;

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
		
		private AWritableFile access;

		
		
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
		
		protected final AWritableFile access()
		{
			return this.access;
		}
		
		protected boolean internalClose()
		{
			if(this.access == null)
			{
				return false;
			}
			
			// release closes implicitely.
			final boolean result = this.access.release();
			this.access = null;
			
			return result;
		}
		
		protected boolean internalOpen()
		{
			if(this.access == null)
			{
				this.access = this.file().useWriting();
			}
			
			return this.access.open();
		}
		
	}
	
}
