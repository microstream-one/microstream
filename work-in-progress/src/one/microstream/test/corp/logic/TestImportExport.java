package one.microstream.test.corp.logic;

import java.io.File;
import java.util.function.Predicate;

import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XSequence;
import one.microstream.files.XFiles;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageConnection;
import one.microstream.storage.types.StorageDataConverterCsvConfiguration;
import one.microstream.storage.types.StorageDataConverterTypeBinaryToCsv;
import one.microstream.storage.types.StorageDataConverterTypeCsvToBinary;
import one.microstream.storage.types.StorageEntityTypeConversionFileProvider;
import one.microstream.storage.types.StorageEntityTypeExportFileProvider;
import one.microstream.storage.types.StorageEntityTypeExportStatistics;
import one.microstream.storage.types.StorageFile;
import one.microstream.storage.types.StorageLockedFile;
import one.microstream.storage.types.StorageTypeDictionary;
import one.microstream.util.cql.CQL;

public class TestImportExport
{
	@SuppressWarnings("unused")
	public static void testExport(final EmbeddedStorageManager storage, final File targetDirectory)
	{
		final StorageConnection storageConnection = storage.createConnection();
		long tStart, tStop;
		final File bin2Dir, csvDir;

		tStart = System.nanoTime();
		final XSequence<File> exportFiles = exportTypes(
			storageConnection,
			XFiles.ensureDirectory(new File(targetDirectory, "bin")),
			"dat"
		);
		tStop = System.nanoTime();
		System.out.println("Data export to binary files complete. Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));

		tStart = System.nanoTime();
		csvDir = convertBinToCsv(storage.typeDictionary(), exportFiles, file -> file.getName().endsWith(".dat"));
		tStop = System.nanoTime();
		System.out.println("Conversion of binary to csv complete. Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));


//		csvDir = new File("D:/BonusExportTest/csv");
//
//		tStart = System.nanoTime();
//		bin2Dir = convertCsvToBin(
//			storage.typeDictionary(),
//			X.List(csvDir.listFiles()),
//			new File(csvDir.getParent(), "bin2"),
//			XFunc.all()
//		);
//		tStop = System.nanoTime();
//		System.out.println("csv2bin done. Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
//
//
//		storage.truncateData();
//
//		bin2Dir = new File("D:/BonusExportTest/bin");
////		bin2Dir = new File("D:/BonusExportTest/bin2");
//
//		tStart = System.nanoTime();
//		storageConnection.importFiles(EqHashEnum.New(bin2Dir.listFiles()));
//		tStop = System.nanoTime();
//		System.out.println("import done. Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));

//		storageConnection.importFiles(EqHashEnum.New(new File("C:/Files/export/bin2/one.microstream.persistence.types.PersistenceRoots$Implementation.dat")));


//		UtilTests.storageCleanup();

//		storage.shutdown();
	}

	static final XSequence<File> exportTypes(
		final StorageConnection storageConnection,
		final File              targetDirectory  ,
		final String            fileSuffix
)
	{
		final StorageEntityTypeExportStatistics result = storageConnection.exportTypes(
			new StorageEntityTypeExportFileProvider.Implementation(targetDirectory, fileSuffix)
		);
		System.out.println(result);

		final XSequence<File> exportFiles = CQL
			.from(result.typeStatistics().values())
			.project(s -> new File(s.file().identifier()))
			.execute()
		;

		return exportFiles;
	}

	protected static File convertBinToCsv(
		final StorageTypeDictionary typeDictionary,
		final XGettingCollection<File> binaryFiles,
		final Predicate<? super File> filter
	)
	{
		final File directory = new File(binaryFiles.get().getParentFile().getParentFile(), "csv");
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.ImplementationUTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Implementation(directory, "csv"),
			typeDictionary,
			null,
			4096,
			4096
		);

		for(final File file : binaryFiles)
		{
			if(!filter.test(file))
			{
				continue;
			}
			try
			{
				final StorageLockedFile storageFile = StorageLockedFile.openLockedFile(file);
				converter.convertDataFile(storageFile);
			}
			catch(final Exception e)
			{
				throw new RuntimeException("Exception while converting file " + file, e);
			}
		}
		return directory;
	}

	static File convertCsvToBin(
		final PersistenceTypeDictionary typeDictionary ,
		final XGettingCollection<File>  binaryFiles    ,
		final File                      targetDirectory,
		final Predicate<? super File>   filter
	)
	{
		final StorageDataConverterTypeCsvToBinary<StorageFile> converter = StorageDataConverterTypeCsvToBinary.New(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			typeDictionary,
			new StorageEntityTypeConversionFileProvider.Implementation(
				targetDirectory, "dat"
			)
		);

		for(final File file : binaryFiles)
		{
			if(!filter.test(file))
			{
				continue;
			}
			try
			{
				final StorageLockedFile storageFile = StorageLockedFile.openLockedFile(file);
				converter.convertCsv(storageFile);
			}
			catch(final Exception e)
			{
				throw new RuntimeException("Exception while converting file "+file, e);
			}
		}


		return targetDirectory;
	}

}
