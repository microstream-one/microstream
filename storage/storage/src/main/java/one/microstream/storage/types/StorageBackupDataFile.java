package one.microstream.storage.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.notNegative;

import one.microstream.afs.types.AFile;

public interface StorageBackupDataFile extends StorageDataFile, StorageBackupChannelFile
{
	public static StorageBackupDataFile New(
		final AFile file        ,
		final int   channelIndex,
		final long  number
	)
	{
		return new StorageBackupDataFile.Default(
			    notNull(file)        ,
			notNegative(channelIndex),
			notNegative(number)
		);
	}
	
	public final class Default extends StorageDataFile.Abstract implements StorageBackupDataFile
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
