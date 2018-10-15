package net.jadoth.storage.io.fs;

import java.io.File;

import net.jadoth.storage.io.ProtageChannelDataFile;

public interface FileSystemChannelDataFile extends FileSystemChannelFile, ProtageChannelDataFile
{
	public final class Implementation
	extends FileSystemFile.Implementation<FileSystemChannelDirectory.Implementation>
	implements FileSystemChannelDataFile
	{
		Implementation(
			final FileSystemChannelDirectory.Implementation directory ,
			final String                                    name      ,
			final File                                      cachedFile
		)
		{
			super(directory, name, cachedFile);
		}

		@Override
		public long number()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageChannelDataFile#number()
		}

		@Override
		public long dataLength()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageChannelDataFile#dataLength()
		}

		@Override
		public boolean isHeadFile()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ProtageChannelDataFile#isHeadFile()
		}

		@Override
		public final int channelIndex()
		{
			return this.directory().channelIndex();
		}
		
	}
}
