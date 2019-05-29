package one.microstream.storage.util;

import java.io.File;
import java.io.IOException;

import one.microstream.chars.VarString;
import one.microstream.collections.XArrays;
import one.microstream.concurrency.XThreads;
import one.microstream.files.XFiles;
import one.microstream.storage.types.StorageTransactionsFileAnalysis;

public class MainUtilTransactionFileConverter
{
	public static void main(final String[] args)
	{
		if(XArrays.hasNoContent(args))
		{
			System.out.println("No transaction file specified. Exiting.");
			XThreads.sleep(1000);
			System.exit(-1);
		}
		
		final File file = new File(args[0]);
		if(!file.exists())
		{
			System.out.println("File not found: " + args[0]);
			XThreads.sleep(1000);
			System.exit(-2);
		}

		System.out.println("Converting transaction entries ...");
		final VarString vs = VarString.New(file.toString()).lf();
		StorageTransactionsFileAnalysis.EntryAssembler.assembleHeader(vs, "\t").lf();
		final VarString s = StorageTransactionsFileAnalysis.Logic.parseFile(file, vs).lf().lf();
		final String result = s.toString();
		System.out.println("Converted String length: " + result.length());
		
		final File outputFile = new File(file.getParentFile(), file.getName() + ".txt");
		System.out.println("Writing File " + outputFile);
		
		try
		{
			XFiles.writeStringToFile(outputFile, result);
		}
		catch(final IOException e)
		{
			e.printStackTrace();
			XThreads.sleep(1000);
			System.exit(-3);
		}

		System.out.println("Done.");
		XThreads.sleep(1000);
		System.exit(0);
	}

}

/*
Use in combination with the following batch script to allow convenience file drag&drop:

@ECHO OFF
REM "%~1" is required to correcly handle spaces and special characters
REM ECHO Executing "%~dp0MicroStreamTransactionFileConverter.jar" "%~1"
ECHO Converting "%~1" ...
REM %~dp0 is the batch file's directory, which has to be specified here because %~1 changes the working directory (or something like that)
java -jar "%~dp0MicroStreamTransactionFileConverter.jar" "%~1"

*/