package one.microstream.storage.types;

import java.util.function.Consumer;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;


// (21.06.2020 TM)FIXME: priv#49: integrate StorageFileProvider in LiveFileProvider, BackupFileProvider, plus ExportFileProvider?
public interface StorageBaseFileProvider
{
	public ADirectory provideChannelDirectory(ADirectory baseDirectory, int channelIndex);
		
	public AFile provideDataFile(ADirectory baseDirectory, int channelIndex, long fileNumber);

	public AFile provideTransactionsFile(ADirectory baseDirectory, int channelIndex);
	

	
	public <F extends StorageDataFile, C extends Consumer<F>> C collectDataFiles(
		ADirectory                 baseDirectory,
		StorageDataFile.Creator<F> creator      ,
		C                          collector    ,
		int                        channelIndex
	);
	
	
	
	public final class Default implements StorageBaseFileProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageDirectoryStructureProvider structureProvider;
		private final StorageFileNameProvider           fileNameProvider ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final StorageDirectoryStructureProvider structureProvider,
			final StorageFileNameProvider           fileNameProvider
		)
		{
			super();
			this.structureProvider = structureProvider;
			this.fileNameProvider  = fileNameProvider ;
		}
		


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		

		@Override
		public final ADirectory provideChannelDirectory(
			final ADirectory parentDirectory,
			final int        hashIndex
		)
		{
			final ADirectory channelDirectory = this.structureProvider.provideChannelDirectory(
				parentDirectory,
				hashIndex,
				this.fileNameProvider
			);
			channelDirectory.ensureExists();
			
			return channelDirectory;
		}
		
		@Override
		public AFile provideDataFile(final ADirectory baseDirectory, final int channelIndex, final long fileNumber)
		{
			final String dataFileName = this.fileNameProvider.provideDataFileName(channelIndex, fileNumber);
			final String dataFileType = this.fileNameProvider.dataFileSuffix();
			final AFile  file         = baseDirectory.ensureFile(dataFileName, dataFileType);
			
			return file;
		}

		@Override
		public AFile provideTransactionsFile(final ADirectory baseDirectory, final int channelIndex)
		{
			final String dataFileName = this.fileNameProvider.provideTransactionsFileName(channelIndex);
			final String dataFileType = this.fileNameProvider.transactionsFileSuffix();
			final AFile  file         = baseDirectory.ensureFile(dataFileName, dataFileType);
			
			return file;
		}
				
		@Override
		public <F extends StorageDataFile, C extends Consumer<F>> C collectDataFiles(
			final ADirectory                 baseDirectory,
			final StorageDataFile.Creator<F> creator      ,
			final C                          collector    ,
			final int                        channelIndex
		)
		{
			baseDirectory.iterateFiles(f ->
			{
				this.fileNameProvider.parseDataInventoryFile(creator, collector, channelIndex, f);
			});
			
			return collector;
		}
			
	}

}
