package one.microstream.storage.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.notNegative;

import one.microstream.afs.AFile;
import one.microstream.storage.types.StorageTransactionsAnalysis.EntryIterator;

public interface StorageLiveTransactionsFile
extends StorageTransactionsFile, StorageLiveChannelFile<StorageLiveTransactionsFile>
{
	@Override
	public default StorageBackupTransactionsFile ensureBackupFile(final StorageBackupInventory backupInventory)
	{
		return backupInventory.ensureTransactionsFile(this);
	}
	
	
	public <P extends EntryIterator> P processBy(P iterator);
	
	
	public static StorageLiveTransactionsFile New(
		final AFile file        ,
		final int   channelIndex
	)
	{
		return new StorageLiveTransactionsFile.Default(
			    notNull(file),
			notNegative(channelIndex)
		);
	}
	
	
	
	
	public final class Default
	extends StorageLiveFile.Abstract<StorageLiveTransactionsFile>
	implements StorageLiveTransactionsFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int channelIndex;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final AFile file, final int channelIndex)
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
		
		@Override
		public <P extends EntryIterator> P processBy(final P iterator)
		{
			StorageTransactionsAnalysis.Logic.processInputFile(
				this.ensureReadable(),
				iterator
			);
			
			return iterator;
		}
		
	}
	
}
