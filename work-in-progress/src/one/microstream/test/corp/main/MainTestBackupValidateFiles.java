package one.microstream.test.corp.main;

import java.io.File;

import one.microstream.collections.BulkList;
import one.microstream.collections.HashTable;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandlerArchiving;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.Storage;
import one.microstream.storage.types.StorageBackupSetup;
import one.microstream.storage.types.StorageLiveFileProvider;
import one.microstream.storage.types.ZStorageNumberedFile;
import one.microstream.util.FileContentComparer;

public class MainTestBackupValidateFiles
{
	static final EmbeddedStorageFoundation<?> FOUNDATION = EmbeddedStorage
		.Foundation(
			Storage.ConfigurationBuilder()
			.setBackupSetup(
				StorageBackupSetup.New(
					Storage
					.FileProviderBuilder()
					.setBackupDirectory("storage/backup")
					.setDeletionDirectory("storage/backup/deleted")
					.setTruncationDirectory("storage/backup/truncated")
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
		final ZStorageNumberedFile transactionFile = storageFileProvider.provideTransactionsFile(
			channelIndex
		);
		final ZStorageNumberedFile backupTranFile = backupSetup.backupFileProvider().provideTransactionsFile(
			channelIndex
		);
		
		final BulkList<ZStorageNumberedFile> dataFiles = storageFileProvider.collectDataFiles(
			BulkList.New(), channelIndex
		);
		
		final HashTable<File, File> fileMapping = HashTable.New();
		fileMapping.add(new File(transactionFile.identifier()), new File(backupTranFile.identifier()));
		
		for(final ZStorageNumberedFile df : dataFiles)
		{
			final File storageFile = new File(df.identifier());
			final ZStorageNumberedFile bf = backupSetup.backupFileProvider().provideDataFile(channelIndex, df.number());
			final File backupFile = new File(bf.identifier());
			fileMapping.add(storageFile, backupFile);
		}
			
		final FileContentComparer fcc = FileContentComparer.New();
		
		fcc.compareFiles(fileMapping);
		System.out.println("Channel " + channelIndex + ":");
		System.out.println(FileContentComparer.Assembler.assemble(fcc.result()));
		System.out.println();
	}
}
