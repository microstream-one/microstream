package one.microstream.persistence.test;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFS;
import one.microstream.afs.AFile;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XSequence;
import one.microstream.functional.XFunc;
import one.microstream.io.XIO;
import one.microstream.storage.types.StorageConnection;

public class MainTestExportConvertImport extends TestStorage
{
	public static void main(final String[] args)
	{
		final NioFileSystem nfs = NioFileSystem.New();
		
		ROOT.set(testGraphEvenMoreManyType());
		final StorageConnection storageConnection = STORAGE.createConnection();
		storageConnection.store(ROOT);
		testExport(nfs.ensureDirectory(XIO.Path("C:/Files/export")));
		exit();
	}

	static void testExport(final ADirectory targetDirectory)
	{
		final StorageConnection storageConnection = STORAGE.createConnection();
		final XSequence<AFile> exportFiles = exportTypes(
			storageConnection,
			AFS.ensureExists(targetDirectory.ensureDirectory("bin")),
			"dat"
		);
		final ADirectory csvDir = convertBinToCsv(exportFiles, file -> file.identifier().endsWith(".dat"));

		final ADirectory bin2Dir = MainTestConvertCsvToBin.convertCsvToBin(
			STORAGE.typeDictionary(),
			targetDirectory.listFiles(),
			csvDir.parent().ensureDirectory("bin2"),
			XFunc.all()
		);

//		STORAGE.truncateData();
		storageConnection.importFiles(EqHashEnum.New(bin2Dir.listFiles()));
//		storageConnection.importFiles(EqHashEnum.New(new File("C:/Files/export/bin2/one.microstream.persistence.types.PersistenceRoots$Default.dat")));

		STORAGE.shutdown();
		MainTestStoragePrintTransactions.printTransactionsFiles(channelCount);

	}

}
