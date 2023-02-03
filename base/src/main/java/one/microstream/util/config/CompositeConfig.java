package one.microstream.util.config;

/*-
 * #%L
 * microstream-base
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

import static one.microstream.X.notNull;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.function.Consumer;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.chars.XCsvParserCharArray;
import one.microstream.chars._charArrayRange;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingMap;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.functional.Aggregator;
import one.microstream.io.XIO;
import one.microstream.typing.KeyValue;
import one.microstream.util.Substituter;
import one.microstream.util.xcsv.XCsvAssembler;
import one.microstream.util.xcsv.XCsvConfiguration;
import one.microstream.util.xcsv.XCsvParser;


public class CompositeConfig
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final ConfigEntry<String> entryString(final String keyName)
	{
		return new ConfigEntryString(keyName);
	}

	public static final ConfigEntry<Boolean> entryBoolean(final String keyName)
	{
		return new ConfigEntryBoolean(keyName);
	}

	public static final ConfigEntry<Integer> entryInteger(final String keyName)
	{
		return new ConfigEntryInteger(keyName);
	}

	public static final ConfigEntry<Long> entryLong(final String keyName)
	{
		return new ConfigEntryLong(keyName);
	}

	public static final ConfigEntry<Double> entryDouble(final String keyName)
	{
		return new ConfigEntryDouble(keyName);
	}

	ConfigEntryAggregator importConfigs(
		final String                     tag       ,
		final XCsvParser<_charArrayRange> parser    ,
		final ConfigEntryAggregator      aggregator,
		final boolean                    mandatory
	)
	{
		final String fileStart  = this.qualifier + '_' + tag + '_';
		final String dotSuffix = XIO.fileSuffixSeparator() + this.filesuffix;

		final File[] configFiles = this.configDirectory.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(final File dir, final String name)
			{
				return name.startsWith(fileStart) && name.endsWith(dotSuffix);
			}
		});

		if(mandatory && (configFiles == null || configFiles.length == 0))
		{
			// (16.07.2013 TM)EXCP: proper exception
			throw new RuntimeException("No suitable files for " + tag + " found in directory " + this.configDirectory);
		}

		for(final File file : configFiles)
		{
			final char[] input;
			try
			{
				input = XIO.readString(file.toPath(), XChars.defaultJvmCharset()).toCharArray();
			}
			catch(final IOException e)
			{
				// (16.07.2013 TM)EXCP: proper exception
				throw new RuntimeException("Could not read file: " + file);
			}
			final String name = file.getName().substring(
				fileStart.length(),
				file.getName().length() - dotSuffix.length()
			);
			parser.parseCsvData(
				this.csvConfig,
				_charArrayRange.New(input),
				aggregator.setNewConfig(name)
			);
		}
		return aggregator;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final String                         rootIdentifier ;
	private final String                         qualifier      ;
	private final String                         filesuffix     ;
	private final XCsvConfiguration              csvConfig      ;
	private final File                           configDirectory;
	private final Substituter<String>            stringCache    ;
	private final EqHashTable<String, SubConfig> subConfigs      = EqHashTable.New();

	private final XGettingMap<String, String>    customVariables    ;
	private final Character                      variableStarter    ;
	private final Character                      variableTerminator;

	private RootConfig defaultConfig;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public CompositeConfig(
		final String                      rootIdentifier    ,
		final String                      qualifier         ,
		final String                      filesuffix        ,
		final XCsvConfiguration            csvConfig         ,
		final File                        configDirectory   ,
		final Substituter<String>         stringCache       ,
		final XGettingMap<String, String> customVariables   ,
		final Character                   variableStarter   ,
		final Character                   variableTerminator
	)
	{
		super();
		this.rootIdentifier     = notNull(rootIdentifier) ;
		this.qualifier          = notNull(qualifier )     ;
		this.filesuffix         = notNull(filesuffix)     ;
		this.csvConfig          = notNull(csvConfig )     ;
		this.configDirectory    = notNull(configDirectory);
		this.stringCache        = notNull(stringCache)    ;
		this.customVariables    = customVariables         ;
		this.variableStarter    = variableStarter         ;
		this.variableTerminator = variableTerminator      ;
	}

	public CompositeConfig(
		final String                      rootIdentifier    ,
		final String                      qualifier         ,
		final String                      filesuffix        ,
		final XCsvConfiguration            csvConfig         ,
		final File                        configDirectory   ,
		final Substituter<String>         stringCache       ,
		final XGettingMap<String, String> customVariables
	)
	{
		this(rootIdentifier, qualifier, filesuffix, csvConfig, configDirectory, stringCache, customVariables, null, null);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public File directory()
	{
		return this.configDirectory;
	}

	private RootConfig createRootConfig()
	{
		final XCsvParser<_charArrayRange> parser = XCsvParserCharArray.New();
		final ConfigEntryAggregator aggregator = ConfigEntryAggregator.New(this.stringCache);
		final RootConfig defaultConfig = new RootConfig(
			this.rootIdentifier    ,
			this.customVariables   ,
			this.variableStarter   ,
			this.variableTerminator
		)
		.updateDefaults(
			this.importConfigs(this.rootIdentifier, parser, aggregator, true).yield()
		)
		;
		return defaultConfig;
	}

	private SubConfig createSubConfig(final String identifier)
	{
		final XCsvParser<_charArrayRange> parser = XCsvParserCharArray.New();
		final ConfigEntryAggregator aggregator = ConfigEntryAggregator.New(this.stringCache);
		final SubConfig config = new SubConfig(
			this.defaultConfig(),
			identifier,
			this.customVariables,
			this.variableStarter,
			this.variableTerminator
		)
		.updateOverrides(
			this.importConfigs(identifier.toString(), parser, aggregator, false).yield()
		);
		return config;
	}

	public synchronized void iterateConfigs(final Consumer<? super AbstractConfig> iterator)
	{
		iterator.accept(this.defaultConfig);
		this.subConfigs.values().iterate(iterator);
	}

	public synchronized RootConfig defaultConfig()
	{
		if(this.defaultConfig == null)
		{
			this.defaultConfig = this.createRootConfig();
		}
		return this.defaultConfig;
	}

	public synchronized SubConfig subConfig(final String identifier)
	{
		SubConfig config = this.subConfigs.get(identifier);
		if(config == null)
		{
			config = this.createSubConfig(identifier);
			this.defaultConfig.register(config);
			this.subConfigs.add(identifier, config);
		}
		return config;
	}


	public synchronized void export()
	{
		final Exporter exporter = new Exporter();
		exporter.accept(this.defaultConfig);
		final XGettingEnum<File> writtenFiles = this.subConfigs.values().iterate(exporter).yield();

		final String fileStart  = this.qualifier + '_';
		final String dotSuffix = XIO.fileSuffixSeparator() + this.filesuffix;
		final File[] filesToDelete = this.configDirectory.listFiles(file ->
		{
			final String filename = file.getName();
			return filename.startsWith(fileStart) && filename.endsWith(dotSuffix) && !writtenFiles.contains(file);

		});

		if(filesToDelete == null)
		{
			return; // x_x @ FindBugs
		}

		for(final File file : filesToDelete)
		{
			if(!file.delete())
			{
				// (16.07.2013 TM)EXCP: proper exception
				throw new RuntimeException("could not delete file: " + file);
			}
		}
	}


	final File buildFile(final String identifier, final ConfigFile config)
	{
		return new File(
			this.configDirectory,
			XIO.addFileSuffix(this.qualifier + '_' + identifier + '_' + config.name, this.filesuffix)
		);
	}


	final File exportConfigFile(final String identifier, final ConfigFile config)
	{
		final File         file           = this.buildFile(identifier, config)      ;
		final VarString    vs             = VarString.New()                         ;
		final XCsvAssembler assembler      = XCsvAssembler.Default.New(this.csvConfig, vs, "\t", " ", "\r", "");

		config.table.iterate(new Consumer<KeyValue<String, String>>()
		{
			@Override
			public void accept(final KeyValue<String, String> element)
			{
				// key is always a simple value and shall be tabbed, a little hacky but okay
				assembler.addRowValueSimple(element.key());
				assembler.addRowValueDelimited(element.value());
				assembler.completeRow();
			}
		});
		assembler.completeRows();

		try
		{
			XIO.write(file.toPath(), vs.toString());
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
		return file;
	}

	final synchronized void export(final AbstractConfig e, final Consumer<File> exportFileCollector)
	{
		final String identifier = e.identifier();
		e.configFiles.values().iterate(new Consumer<ConfigFile>()
		{
			@Override
			public void accept(final ConfigFile e)
			{
				final File exportFile = CompositeConfig.this.exportConfigFile(identifier, e);
				exportFileCollector.accept(exportFile);
			}
		});
	}


	final class Exporter implements Aggregator<AbstractConfig, XGettingEnum<File>>
	{
		final EqHashEnum<File> files = EqHashEnum.New();

		@Override
		public final void accept(final AbstractConfig e)
		{
			CompositeConfig.this.export(e, this.files);
		}

		@Override
		public final XGettingEnum<File> yield()
		{
			return this.files;
		}
	}

}
