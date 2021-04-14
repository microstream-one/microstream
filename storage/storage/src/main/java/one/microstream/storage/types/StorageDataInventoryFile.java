package one.microstream.storage.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.notNegative;

import one.microstream.afs.types.AFile;


public interface StorageDataInventoryFile extends StorageDataFile
{
	@Override
	public AFile file();
	
	@Override
	public int channelIndex();
	
	@Override
	public long number();
	
	
	
	@FunctionalInterface
	public interface Creator extends StorageDataFile.Creator<StorageDataInventoryFile>
	{
		@Override
		public StorageDataInventoryFile createDataFile(AFile file, int channelIndex, long number);
	}
	
	
	public static StorageDataInventoryFile New(
		final AFile file        ,
		final int   channelIndex,
		final long  number
	)
	{
		return new StorageDataInventoryFile.Default(
			    notNull(file),
			notNegative(channelIndex),
			notNegative(number)
		);
	}
	
	public class Default extends StorageDataFile.Abstract implements StorageDataInventoryFile
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final AFile file, final int channelIndex, final long number)
		{
			super(file, channelIndex, number);
		}
				
	}
}
