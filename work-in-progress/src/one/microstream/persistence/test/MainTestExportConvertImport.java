package one.microstream.persistence.test;

import java.nio.file.Path;

import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XSequence;
import one.microstream.functional.XFunc;
import one.microstream.io.XIO;
import one.microstream.storage.types.StorageConnection;

public class MainTestExportConvertImport extends TestStorage
{
	public static void main(final String[] args)
	{
		ROOT.set(testGraphEvenMoreManyType());
		final StorageConnection storageConnection = STORAGE.createConnection();
		storageConnection.store(ROOT);
		testExport(XIO.Path("C:/Files/export"));
		exit();
	}

	static void testExport(final Path targetDirectory)
	{
		final StorageConnection storageConnection = STORAGE.createConnection();
		final XSequence<Path> exportFiles = exportTypes(
			storageConnection,
			XIO.unchecked.ensureDirectory(XIO.Path(targetDirectory, "bin")),
			"dat"
		);
		final Path csvDir = convertBinToCsv(exportFiles, file -> XIO.getFileName(file).endsWith(".dat"));

		final Path bin2Dir = MainTestConvertCsvToBin.convertCsvToBin(
			STORAGE.typeDictionary(),
			XIO.unchecked.listEntries(targetDirectory, BulkList.New()),
			XIO.Path(csvDir.getParent(), "bin2"),
			XFunc.all()
		);

//		STORAGE.truncateData();
		storageConnection.importFiles(EqHashEnum.New(XIO.unchecked.listEntries(bin2Dir)));
//		storageConnection.importFiles(EqHashEnum.New(new File("C:/Files/export/bin2/one.microstream.persistence.types.PersistenceRoots$Default.dat")));

		STORAGE.shutdown();
		MainTestStoragePrintTransactions.printTransactionsFiles(channelCount);

	}

}
