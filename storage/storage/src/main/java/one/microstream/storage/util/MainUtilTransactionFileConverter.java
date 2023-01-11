package one.microstream.storage.util;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import one.microstream.afs.nio.types.NioFileSystem;
import one.microstream.afs.types.AFS;
import one.microstream.afs.types.AFile;
import one.microstream.chars.VarString;
import one.microstream.collections.XArrays;
import one.microstream.concurrency.XThreads;
import one.microstream.io.XIO;
import one.microstream.storage.types.StorageTransactionsAnalysis;


/**
 * Tiny utility class that allows conversion of transaction files into a human readable form.
 * Search internal confluence for "Transaction File Converter" for a helper .bat file and a guide.
 *
 */
public class MainUtilTransactionFileConverter
{
	/* (04.05.2020 TM)NOTE:
	 * Since this is a tiny standalone utility program that is not called from "primary" framework code,
	 * it intentionally uses naive sys-outs and exception printing.
	 * So if code analysis tools pick on it, please ignore them.
	 */
	public static void main(final String[] args)
	{
		if(XArrays.hasNoContent(args))
		{
			System.out.println("No transaction file specified. Exiting.");
			XThreads.sleep(1000);
			System.exit(-1);
		}
		
		final AFile file = NioFileSystem.New().ensureFile(XIO.Path(args[0]));
		if(!file.exists())
		{
			System.out.println("File not found: " + args[0]);
			XThreads.sleep(1000);
			System.exit(-2);
		}

		System.out.println("Converting transaction entries ...");
		final VarString vs = VarString.New(file.toString()).lf();
		StorageTransactionsAnalysis.EntryAssembler.assembleHeader(vs, "\t").lf();
		final VarString s = StorageTransactionsAnalysis.Logic.parseFile(file, vs).lf().lf();
		final String result = s.toString();
		System.out.println("Converted String length: " + result.length());
		
		final AFile outputFile = file.parent().ensureFile(file.name(), "txt");
		System.out.println("Writing File " + outputFile);
		
		try
		{
			AFS.writeString(outputFile, result);
		}
		catch(final Exception e)
		{
			// naive printing is okay for a tiny standalone-utility program.
			e.printStackTrace(); // NOSONAR
			
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
REM "%~1" is required to correctly handle spaces and special characters
REM ECHO Executing "%~dp0MicroStreamTransactionFileConverter.jar" "%~1"
ECHO Converting "%~1" ...
REM %~dp0 is the batch file's directory, which has to be specified here because %~1 changes the working directory (or something like that)
java -jar "%~dp0MicroStreamTransactionFileConverter.jar" "%~1"

*/
