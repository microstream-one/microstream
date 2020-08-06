package one.microstream.test.corp.logic;

import java.util.function.Predicate;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFS;
import one.microstream.afs.AFile;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XSequence;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageConnection;
import one.microstream.storage.types.StorageDataConverterCsvConfiguration;
import one.microstream.storage.types.StorageDataConverterTypeBinaryToCsv;
import one.microstream.storage.types.StorageDataConverterTypeCsvToBinary;
import one.microstream.storage.types.StorageEntityTypeConversionFileProvider;
import one.microstream.storage.types.StorageEntityTypeExportFileProvider;
import one.microstream.storage.types.StorageEntityTypeExportStatistics;
import one.microstream.storage.types.StorageFileNameProvider;
import one.microstream.storage.types.StorageTypeDictionary;
import one.microstream.util.cql.CQL;

public class TestImportExport
{
	static final String DAT = StorageFileNameProvider.Defaults.defaultDataFileSuffix();
	
	@SuppressWarnings("unused")
	public static void testExport(final EmbeddedStorageManager storage, final ADirectory targetDirectory)
	{
		final StorageConnection storageConnection = storage.createConnection();
		long tStart, tStop;
		final ADirectory bin2Dir, csvDir;

		tStart = System.nanoTime();
		final XSequence<AFile> exportFiles = exportTypes(
			storageConnection,
			AFS.ensureExists(targetDirectory.ensureDirectory("bin")),
			"dat"
		);
		tStop = System.nanoTime();
		System.out.println("Data export to binary files complete. Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));

		tStart = System.nanoTime();
		csvDir = convertBinToCsv(storage.typeDictionary(), exportFiles, file -> DAT.equals(file.type()));
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

	static final XSequence<AFile> exportTypes(
		final StorageConnection storageConnection,
		final ADirectory        targetDirectory  ,
		final String            fileSuffix
)
	{
		final StorageEntityTypeExportStatistics result = storageConnection.exportTypes(
			new StorageEntityTypeExportFileProvider.Default(targetDirectory, fileSuffix)
		);
		System.out.println(result);

		final XSequence<AFile> exportFiles = CQL
			.from(result.typeStatistics().values())
			.project(s -> s.file())
			.execute()
		;

		return exportFiles;
	}

	protected static ADirectory convertBinToCsv(
		final StorageTypeDictionary typeDictionary,
		final XGettingCollection<AFile> binaryFiles,
		final Predicate<? super AFile> filter
	)
	{
		final ADirectory directory = binaryFiles.get().parent().parent().ensureDirectory("csv");
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.UTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Default(directory, "csv"),
			typeDictionary,
			null,
			4096,
			4096
		);

		for(final AFile file : binaryFiles)
		{
			if(!filter.test(file))
			{
				continue;
			}
			try
			{
				AFS.execute(file, rf -> converter.convertDataFile(rf));
			}
			catch(final Exception e)
			{
				throw new RuntimeException("Exception while converting file " + file, e);
			}
		}
		return directory;
	}

	static ADirectory convertCsvToBin(
		final PersistenceTypeDictionary typeDictionary ,
		final XGettingCollection<AFile> binaryFiles    ,
		final ADirectory                targetDirectory,
		final Predicate<? super AFile>  filter
	)
	{
		final StorageDataConverterTypeCsvToBinary<AFile> converter = StorageDataConverterTypeCsvToBinary.New(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			typeDictionary,
			new StorageEntityTypeConversionFileProvider.Default(
				targetDirectory, "dat"
			)
		);

		for(final AFile file : binaryFiles)
		{
			if(!filter.test(file))
			{
				continue;
			}
			try
			{
				AFS.execute(file, rf -> converter.convertCsv(rf));
			}
			catch(final Exception e)
			{
				throw new RuntimeException("Exception while converting file "+file, e);
			}
		}


		return targetDirectory;
	}

}
