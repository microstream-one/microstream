package one.microstream.storage.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.notNegative;

import one.microstream.afs.AFile;


// (15.06.2020 TM)FIXME: priv#49: delete if really not necessary
public interface StorageDataInventoryFile extends StorageHashChannelPart
{
	public AFile file();
	
	@Override
	public int channelIndex();
	
	public long fileNumber();
	
	
	
	public static StorageDataInventoryFile New(
		final AFile file        ,
		final int   channelIndex,
		final long  fileNumber
	)
	{
		return new StorageDataInventoryFile.Default(
			    notNull(file),
			notNegative(channelIndex),
			notNegative(fileNumber)
		);
	}
	
	public class Default implements StorageDataInventoryFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		AFile file        ;
		int   channelIndex;
		long  fileNumber  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final AFile file, final int channelIndex, final long fileNumber)
		{
			super();
			this.file         = file        ;
			this.channelIndex = channelIndex;
			this.fileNumber   = fileNumber  ;
		}
		

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public AFile file()
		{
			return this.file;
		}

		@Override
		public int channelIndex()
		{
			return this.channelIndex;
		}

		@Override
		public long fileNumber()
		{
			return this.fileNumber;
		}
		
	}
}
