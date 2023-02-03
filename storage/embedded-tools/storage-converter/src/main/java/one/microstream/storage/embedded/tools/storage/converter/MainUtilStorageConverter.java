package one.microstream.storage.embedded.tools.storage.converter;

/*-
 * #%L
 * MicroStream Embedded Storage Tools Converter
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

import java.io.File;

import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfiguration;
import one.microstream.storage.types.StorageConfiguration;

/**
 * Utility class that allows to convert a storage into another
 * one. The current implementation can change channel count only
 * and is limited to the systems default NioFileSystem
 * because of the missing possibility to define the StorageFileSystem
 * in configuration files.
 * If the file system has to be changed the {@link StorageConverter} class
 * may be used in a custom converter implementation.
 * Existing backups will not be converted!
 *
 */
public class MainUtilStorageConverter
{
	public static String HELP =
		"Convert a storage into a new one. The source and the new target storage \n"
		+ "must be specified in MicroStream config files provided as program arguments: \n"
		+ "\n"
		+ "MainUtilStorageConverter sourceConfig.xml targetConfig.xml"
		+ "\n"
		;
					
	public static void main(final String[] args)
	{
		verifyArguments(args);
		
		final String srcConfigFile = args[0];
		final String dstConfigFile = args[1];
		
		final StorageConfiguration sourceConfig = EmbeddedStorageConfiguration.load(srcConfigFile)
			.createEmbeddedStorageFoundation().getConfiguration();
		
		final StorageConfiguration targetConfig = EmbeddedStorageConfiguration.load(dstConfigFile)
			.createEmbeddedStorageFoundation().getConfiguration();
		
		
		System.out.println("Source storage configuration: " + srcConfigFile);
		System.out.println("Target storage configuration: " + dstConfigFile);
		
		final StorageConverter storageConverter = new StorageConverter(sourceConfig, targetConfig);
		storageConverter.start();
		
		System.out.println("Storage conversion finished!");
	}

	private static void verifyArguments(final String[] args)
	{
		if(args.length == 2)
		{
			if(new File(args[0]).canRead())
			{
				if(new File(args[1]).canRead())
				{
					return;
				}
				else
				{
					System.err.println("Can't read file " + args[1]);
				}
			}
			else
			{
				System.err.println("Can't read file " + args[0]);
			}
		}
		else
		{
			System.err.println("Invalid number of arguments.");
		}
		
		System.out.println(HELP);
		System.exit(-1);
	}

}
