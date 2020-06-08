package one.microstream.test.corp.logic;

import java.nio.file.Path;
import java.util.function.Predicate;

import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XSequence;
import one.microstream.io.XIO;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageConnection;
import one.microstream.storage.types.StorageDataConverterCsvConfiguration;
import one.microstream.storage.types.StorageDataConverterTypeBinaryToCsv;
import one.microstream.storage.types.StorageDataConverterTypeCsvToBinary;
import one.microstream.storage.types.StorageEntityTypeConversionFileProvider;
import one.microstream.storage.types.StorageEntityTypeExportFileProvider;
import one.microstream.storage.types.StorageEntityTypeExportStatistics;
import one.microstream.storage.types.ZStorageFile;
import one.microstream.storage.types.ZStorageLockedFile;
import one.microstream.storage.types.StorageTypeDictionary;
import one.microstream.util.cql.CQL;

public class TestImportExport
{
	@SuppressWarnings("unused")
	public static void testExport(final EmbeddedStorageManager storage, final Path targetDirectory)
	{
		final StorageConnection storageConnection = storage.createConnection();
		long tStart, tStop;
		final Path bin2Dir, csvDir;

		tStart = System.nanoTime();
		final XSequence<Path> exportFiles = exportTypes(
			storageConnection,
			XIO.unchecked.ensureDirectory(XIO.Path(targetDirectory, "bin")),
			"dat"
		);
		tStop = System.nanoTime();
		System.out.println("Data export to binary files complete. Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));

		tStart = System.nanoTime();
		csvDir = convertBinToCsv(storage.typeDictionary(), exportFiles, file -> XIO.getFileName(file).endsWith(".dat"));
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

//		storageConnection.importFiles(EqHashEnum.New(new File("C:/Files/export/bin2/one.microstream.persistence.types.PersistenceRoots$Default.dat")));


//		UtilTests.storageCleanup();

//		storage.shutdown();
	}

	static final XSequence<Path> exportTypes(
		final StorageConnection storageConnection,
		final Path              targetDirectory  ,
		final String            fileSuffix
)
	{
		final StorageEntityTypeExportStatistics result = storageConnection.exportTypes(
			new StorageEntityTypeExportFileProvider.Default(targetDirectory, fileSuffix)
		);
		System.out.println(result);

		final XSequence<Path> exportFiles = CQL
			.from(result.typeStatistics().values())
			.project(s -> XIO.Path(s.file().identifier()))
			.execute()
		;

		return exportFiles;
	}

	protected static Path convertBinToCsv(
		final StorageTypeDictionary typeDictionary,
		final XGettingCollection<Path> binaryFiles,
		final Predicate<? super Path> filter
	)
	{
		final Path directory = XIO.Path(binaryFiles.get().getParent().getParent(), "csv");
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.UTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Default(directory, "csv"),
			typeDictionary,
			null,
			4096,
			4096
		);

		for(final Path file : binaryFiles)
		{
			if(!filter.test(file))
			{
				continue;
			}
			try
			{
				final ZStorageLockedFile storageFile = ZStorageLockedFile.openLockedFile(file);
				converter.convertDataFile(storageFile);
			}
			catch(final Exception e)
			{
				throw new RuntimeException("Exception while converting file " + file, e);
			}
		}
		return directory;
	}

	static Path convertCsvToBin(
		final PersistenceTypeDictionary typeDictionary ,
		final XGettingCollection<Path>  binaryFiles    ,
		final Path                      targetDirectory,
		final Predicate<? super Path>   filter
	)
	{
		final StorageDataConverterTypeCsvToBinary<ZStorageFile> converter = StorageDataConverterTypeCsvToBinary.New(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			typeDictionary,
			new StorageEntityTypeConversionFileProvider.Default(
				targetDirectory, "dat"
			)
		);

		for(final Path file : binaryFiles)
		{
			if(!filter.test(file))
			{
				continue;
			}
			try
			{
				final ZStorageLockedFile storageFile = ZStorageLockedFile.openLockedFile(file);
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
