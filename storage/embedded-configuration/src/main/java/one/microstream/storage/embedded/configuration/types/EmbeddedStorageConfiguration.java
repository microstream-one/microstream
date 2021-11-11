
package one.microstream.storage.embedded.configuration.types;

/*-
 * #%L
 * microstream-storage-embedded-configuration
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import static one.microstream.X.coalesce;

import java.nio.charset.Charset;

import one.microstream.configuration.exceptions.ConfigurationExceptionNoConfigurationFound;
import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationLoader;
import one.microstream.configuration.types.ConfigurationParser;
import one.microstream.configuration.types.ConfigurationParserIni;
import one.microstream.configuration.types.ConfigurationParserXml;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;

/**
 * Static utility class containing various utility methods to create and load storage configurations.
 * 
 * @see EmbeddedStorageConfigurationBuilder
 * @since 05.00.00
 *
 */
public final class EmbeddedStorageConfiguration
{
	/**
	 * The property name which is used to hand the external configuration file path to the application.
	 * <p>
	 * Either as system property or in the context's configuration, e.g. Spring's application.properties.
	 *
	 * @return "microstream.storage.configuration.path"
	 */
	public static String PathProperty()
	{
		return "microstream.storage.configuration.path";
	}

	/**
	 * The default name of the storage configuration resource.
	 *
	 * @see #load()
	 *
	 * @return "microstream-storage.properties"
	 */
	public static String DefaultResourceName()
	{
		return "microstream-storage.properties";
	}
	
	/**
	 * Creates a new {@link EmbeddedStorageConfigurationBuilder} instance.
	 * <p>
	 * This is a synonym for <code>EmbeddedStorageConfigurationBuilder.New()</code>.
	 * 
	 * @return a newly created {@link EmbeddedStorageConfigurationBuilder}
	 * @see EmbeddedStorageConfigurationBuilder#New()
	 */
	public static EmbeddedStorageConfigurationBuilder Builder()
	{
		return EmbeddedStorageConfigurationBuilder.New();
	}
	
	/**
	 * Tries to load the default storage configuration properties file.
	 * <p>
	 * The resource name is either set via the system property "microstream.storage.configuration.path"
	 * or the default name "microstream-storage.properties".
	 * <p>
	 * The search order is as described in {@link ConfigurationLoader#New(String)}.
	 * 
	 * @return the loaded configuration builder
	 * @throws ConfigurationExceptionNoConfigurationFound if no configuration can be found at the given path
	 * @see #PathProperty()
	 * @see #DefaultResourceName()
	 */
	public static EmbeddedStorageConfigurationBuilder load()
	{
		return load(
			coalesce(System.getProperty(PathProperty()), DefaultResourceName()),
			ConfigurationLoader.Defaults.defaultCharset()
		);
	}
	
	/**
	 * Tries to load the storage configuration file from <code>path</code>.
	 * <p>
	 * Depending on the file suffix either the XML or the INI loader is used.
	 * <p>
	 * The search order is as described in {@link ConfigurationLoader#New(String)}.
	 * 
	 * @param path the path to load the configuration from
	 * @return the loaded configuration builder
	 * @throws ConfigurationExceptionNoConfigurationFound if no configuration can be found at the given path
	 * @see #PathProperty()
	 * @see #DefaultResourceName()
	 */
	public static EmbeddedStorageConfigurationBuilder load(
		final String path
	)
	{
		return load(
			path                                         ,
			ConfigurationLoader.Defaults.defaultCharset()
		);
	}
	
	/**
	 * Tries to load the default storage configuration properties file.
	 * <p>
	 * The resource name is either set via the system property "microstream.storage.configuration.path"
	 * or the default name "microstream-storage.properties".
	 * <p>
	 * The search order is as described in {@link ConfigurationLoader#New(String)}.
	 * 
	 * @param charset the charset used to parse the configuration
	 * @return the loaded configuration builder
	 * @throws ConfigurationExceptionNoConfigurationFound if no configuration can be found at the given path
	 * @see #PathProperty()
	 * @see #DefaultResourceName()
	 */
	public static EmbeddedStorageConfigurationBuilder load(
		final Charset charset
	)
	{
		return load(
			coalesce(System.getProperty(PathProperty()), DefaultResourceName()),
			charset
		);
	}
	
	/**
	 * Tries to load the storage configuration file from <code>path</code>.
	 * <p>
	 * Depending on the file suffix either the XML or the INI loader is used.
	 * <p>
	 * The search order is as described in {@link ConfigurationLoader#New(String)}.
	 * 
	 * @param path the path to load the configuration from
	 * @param charset the charset used to parse the configuration
	 * @return the loaded configuration builder
	 * @throws ConfigurationExceptionNoConfigurationFound if no configuration can be found at the given path
	 * @see #PathProperty()
	 * @see #DefaultResourceName()
	 */
	public static EmbeddedStorageConfigurationBuilder load(
		final String  path   ,
		final Charset charset
	)
	{
		return load(
			ConfigurationLoader.New(path, charset),
			path.toLowerCase().endsWith(".xml")
				? ConfigurationParserXml.New()
				: ConfigurationParserIni.New()
		);
	}
	
	/**
	 * Tries to load the storage configuration based on a given loader and parser.
	 * 
	 * @param loader the loader to use
	 * @param parser the parser to use
	 * @return the loaded configuration builder
	 */
	public static EmbeddedStorageConfigurationBuilder load(
		final ConfigurationLoader loader,
		final ConfigurationParser parser
	)
	{
		return EmbeddedStorageConfigurationBuilder.New(
			Configuration.Builder().load(loader, parser)
		);
	}
		
	/**
	 * Creates a new {@link EmbeddedStorageFoundation.Creator} based on a {@link Configuration}.
	 * @param configuration the configuration the foundation will be based on
	 * @return a new {@link EmbeddedStorageFoundation.Creator}
	 */
	public static EmbeddedStorageFoundation.Creator FoundationCreator(
		final Configuration configuration
	)
	{
		return EmbeddedStorageFoundationCreatorConfigurationBased.New(configuration);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 *
	 * @throws UnsupportedOperationException
	 */
	private EmbeddedStorageConfiguration()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
