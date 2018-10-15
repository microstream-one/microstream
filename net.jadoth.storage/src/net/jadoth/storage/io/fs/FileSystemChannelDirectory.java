package net.jadoth.storage.io.fs;

import java.io.File;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.storage.io.ProtageChannelDataFile;
import net.jadoth.storage.io.ProtageChannelDirectory;

public interface FileSystemChannelDirectory extends FileSystemDirectory, ProtageChannelDirectory
{

	@Override
	public FileSystemChannelFile createFile(String fileName);
	
	public final class Implementation extends FileSystemDirectory.Implementation implements FileSystemChannelDirectory
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int channelIndex                ;
		private       int currentHighestDataFileNumber;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final File                                                 directory                   ,
			final String                                               name                        ,
			final EqHashTable<String, FileSystemFile.Implementation>   files                       ,
			final XGettingTable<String, FileSystemFile.Implementation> viewFiles                   ,
			final int                                                  channelIndex                ,
			final int                                                  currentHighestDataFileNumber
		)
		{
			super(directory, name, files, viewFiles);
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
		public synchronized final FileSystemChannelFile createFile(final String fileName)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME FileSystemChannelDirectory.Implementation#createFile()
		}
		
		@Override
		public ProtageChannelDataFile createNextDataFile(final String fileName)
		{
			final String identifiedFileName = fileName
				+ '_' + this.channelIndex
				+ '_' + this.currentHighestDataFileNumber
			;
			
			final File file = this.internalCreateFile(identifiedFileName);
			final FileSystemChannelDataFile.Implementation dataFile = new FileSystemChannelDataFile.Implementation(this, identifiedFileName, file);
			
			// FIXME FileSystemChannelDirectory.Implementation#createNextDataFile()
			throw new net.jadoth.meta.NotImplementedYetError();
//			return this.internalRegisterFile(dataFile);
		}
		
	}
}
