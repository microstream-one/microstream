package net.jadoth.persistence.test;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.storage.types.StorageDataFileItemIterator;
import net.jadoth.storage.types.StorageDataFileItemIterator.ItemProcessor;
import net.jadoth.storage.types.StorageLockedFile;

public class MainTestPrintDataFileInventory
{
	public static void main(final String[] args) throws IOException
	{
		final FileChannel fc = StorageLockedFile.openLockedFile(
			new File("D:/Bonus25/storage_2015-03-19 ID Fehler lokal/graveyard1/channel_1_129.dat")
		).channel();
		StorageDataFileItemIterator.Implementation.processInputFile(fc, new DataFileInventoryPrinter());
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
