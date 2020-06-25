package one.microstream.storage.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.notNegative;

import one.microstream.afs.AFile;

public interface StorageBackupTransactionsFile extends StorageTransactionsFile, StorageBackupChannelFile
{
	public static StorageBackupTransactionsFile New(
		final AFile file        ,
		final int   channelIndex
	)
	{
		return new StorageBackupTransactionsFile.Default(
			    notNull(file)        ,
			notNegative(channelIndex)
		);
	}
	
	public final class Default extends StorageChannelFile.Abstract implements StorageBackupTransactionsFile
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(final AFile file, final int channelIndex)
		{
			super(file, channelIndex);
		}
		
	}
}
