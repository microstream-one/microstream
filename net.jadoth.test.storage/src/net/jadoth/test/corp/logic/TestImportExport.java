package net.jadoth.test.corp.logic;

import java.io.File;
import java.util.function.Predicate;

import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XSequence;
import net.jadoth.files.XFiles;
import net.jadoth.persistence.types.PersistenceTypeDictionary;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.storage.types.StorageConnection;
import net.jadoth.storage.types.StorageDataConverterCsvConfiguration;
import net.jadoth.storage.types.StorageDataConverterTypeBinaryToCsv;
import net.jadoth.storage.types.StorageDataConverterTypeCsvToBinary;
import net.jadoth.storage.types.StorageEntityTypeConversionFileProvider;
import net.jadoth.storage.types.StorageEntityTypeExportFileProvider;
import net.jadoth.storage.types.StorageEntityTypeExportStatistics;
import net.jadoth.storage.types.StorageTypeDictionary;
import net.jadoth.util.cql.CQL;

public class TestImportExport
{
	@SuppressWarnings("unused")
	static void testExport(final EmbeddedStorageManager storage, final File targetDirectory)
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
		System.out.println("Bin exp done. Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));

		tStart = System.nanoTime();
		csvDir = convertBinToCsv(storage.typeDictionary(), exportFiles, file -> file.getName().endsWith(".dat"));
		tStop = System.nanoTime();
		System.out.println("bin2csv done. Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));


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

//		storageConnection.importFiles(EqHashEnum.New(new File("C:/Files/export/bin2/net.jadoth.persistence.types.PersistenceRoots$Implementation.dat")));


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
			.project(s -> s.file().file())
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
		final File dir = new File(binaryFiles.get().getParentFile().getParentFile(), "csv");
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.ImplementationUTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Implementation(dir, "csv"),
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
				converter.convertDataFile(file);
			}
			catch(final Exception e)
			{
				throw new RuntimeException("Exception while converting file "+file, e);
			}
		}
		return dir;
	}

	static File convertCsvToBin(
		final PersistenceTypeDictionary typeDictionary ,
		final XGettingCollection<File>  binaryFiles    ,
		final File                      targetDirectory,
		final Predicate<? super File>   filter
	)
	{
		final StorageDataConverterTypeCsvToBinary<File> converter = StorageDataConverterTypeCsvToBinary.New(
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
				converter.convertCsv(file);
			}
			catch(final Exception e)
			{
				throw new RuntimeException("Exception while converting file "+file, e);
			}
		}


		return targetDirectory;
	}

}
