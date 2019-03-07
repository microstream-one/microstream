package net.jadoth.persistence.test;

import java.io.File;

import net.jadoth.X;
import net.jadoth.collections.EqHashEnum;
import net.jadoth.collections.types.XSequence;
import net.jadoth.files.XFiles;
import net.jadoth.functional.XFunc;
import net.jadoth.storage.types.StorageConnection;

public class MainTestExportConvertImport extends TestStorage
{
	public static void main(final String[] args)
	{
		ROOT.set(testGraphEvenMoreManyType());
		final StorageConnection storageConnection = STORAGE.createConnection();
		storageConnection.store(ROOT);
		testExport(new File("C:/Files/export"));
		exit();
	}

	static void testExport(final File targetDirectory)
	{
		final StorageConnection storageConnection = STORAGE.createConnection();
		final XSequence<File> exportFiles = exportTypes(
			storageConnection,
			XFiles.ensureDirectory(new File(targetDirectory, "bin")),
			"dat"
		);
		final File csvDir = convertBinToCsv(exportFiles, file -> file.getName().endsWith(".dat"));

		final File bin2Dir = MainTestConvertCsvToBin.convertCsvToBin(
			STORAGE.typeDictionary(),
			X.List(csvDir.listFiles()),
			new File(csvDir.getParent(), "bin2"),
			XFunc.all()
		);

//		STORAGE.truncateData();
		storageConnection.importFiles(EqHashEnum.New(bin2Dir.listFiles()));
//		storageConnection.importFiles(EqHashEnum.New(new File("C:/Files/export/bin2/net.jadoth.persistence.types.PersistenceRoots$Implementation.dat")));

		STORAGE.shutdown();
		MainTestStoragePrintTransactions.printTransactionsFiles(channelCount);

	}

}
