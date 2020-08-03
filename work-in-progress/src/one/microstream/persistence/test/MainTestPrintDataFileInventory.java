package one.microstream.persistence.test;

import java.io.IOException;

import one.microstream.afs.AReadableFile;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.io.XIO;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.storage.types.StorageDataFileItemIterator;
import one.microstream.storage.types.StorageDataFileItemIterator.ItemProcessor;

public class MainTestPrintDataFileInventory
{
	public static void main(final String[] args) throws IOException
	{
		final NioFileSystem nfs = NioFileSystem.New();
		final AReadableFile rFile = nfs.ensureFile(XIO.Path(
			"D:/Bonus25/storage_2015-03-19 ID Fehler lokal/graveyard1/channel_1_129.dat"
		)).useReading();
		StorageDataFileItemIterator.Default.processInputFile(rFile, new DataFileInventoryPrinter());
	}


	static final class DataFileInventoryPrinter implements ItemProcessor
	{
		private long fileOffset = 0;

		@Override
		public boolean accept(final long address, final long availableItemLength)
		{
			final long length = Binary.getEntityLengthRawValue(address);

			// check for a gap
			if(length < 0)
			{
//				DEBUGStorage.println("Gap    @"+this.currentSourceFilePosition+" ["+ -length +"]");
				System.out.println(this.fileOffset+"\t"+length+"\t\t");

				this.fileOffset -= length;
				return true;
			}

			if(availableItemLength < Binary.entityHeaderLength())
			{
				// signal to calling context that entity cannot be processed and header must be reloaded
				return false;
			}

			final long objectId = Binary.getEntityObjectIdRawValue(address);
			final long typeId   = Binary.getEntityTypeIdRawValue(address);

			System.out.println(this.fileOffset+"\t"+length+"\t"+typeId+"\t"+objectId);

			this.fileOffset += length;
			return true;
		}

	}
}
