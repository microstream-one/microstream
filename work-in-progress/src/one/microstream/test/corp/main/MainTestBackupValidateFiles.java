package one.microstream.test.corp.main;

import one.microstream.afs.AFile;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.collections.BulkList;
import one.microstream.collections.HashTable;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandlerArchiving;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.Storage;
import one.microstream.storage.types.StorageBackupDataFile;
import one.microstream.storage.types.StorageBackupSetup;
import one.microstream.storage.types.StorageBackupTransactionsFile;
import one.microstream.storage.types.StorageDataInventoryFile;
import one.microstream.storage.types.StorageLiveFileProvider;
import one.microstream.util.FileContentComparer;

public class MainTestBackupValidateFiles
{
	static final NioFileSystem FS = NioFileSystem.New();
	static final EmbeddedStorageFoundation<?> FOUNDATION = EmbeddedStorage
		.Foundation(
			Storage.ConfigurationBuilder()
			.setBackupSetup(
				StorageBackupSetup.New(
					Storage
					.BackupFileProviderBuilder()
					.setDirectory(FS.ensureDirectoryPath("storage", "backup"))
					.setDeletionDirectory(FS.ensureDirectoryPath("storag", "backup", "deleted"))
					.setTruncationDirectory(FS.ensureDirectoryPath("storage", "backup", "truncated"))
					.setFileHandlerCreator(PersistenceTypeDictionaryFileHandlerArchiving::New)
					.createFileProvider()
				)
			)
		)
	;
	
	public static void main(final String[] args)
	{
		validateBackupFiles(FOUNDATION);
	}
	
	static void validateBackupFiles(final EmbeddedStorageFoundation<?> foundation)
	{
		final StorageBackupSetup backupSetup = foundation.getConfiguration().backupSetup();
		if(backupSetup == null)
		{
			throw new RuntimeException("No backup defined");
		}
		
		final int channelCount = foundation.getConfiguration().channelCountProvider().getChannelCount();
		for(int i = 0; i < channelCount; i++)
		{
			validateChannelFiles(foundation.getConfiguration().fileProvider(), backupSetup, i);
		}
	}
	
	static void validateChannelFiles(
		final StorageLiveFileProvider storageFileProvider,
		final StorageBackupSetup  backupSetup        ,
		final int                 channelIndex
	)
	{
		final AFile transactionFile = storageFileProvider.provideTransactionsFile(
			channelIndex
		);
		final StorageBackupTransactionsFile backupTFile = backupSetup.backupFileProvider().provideBackupTransactionsFile(
			channelIndex
		);
		
		final BulkList<StorageDataInventoryFile> dataFiles = storageFileProvider.collectDataFiles(
			StorageDataInventoryFile::New, BulkList.New(), channelIndex
		);
		
		final HashTable<AFile, AFile> fileMapping = HashTable.New();
		fileMapping.add(transactionFile, backupTFile.file());
		
		for(final StorageDataInventoryFile df : dataFiles)
		{
			final StorageBackupDataFile bf = backupSetup.backupFileProvider().provideBackupDataFile(channelIndex, df.number());
			fileMapping.add(df.file(), bf.file());
		}
			
		final FileContentComparer fcc = FileContentComparer.New();
		
		fcc.compareFiles(fileMapping);
		System.out.println("Channel " + channelIndex + ":");
		System.out.println(FileContentComparer.Assembler.assemble(fcc.result()));
		System.out.println();
	}
}
