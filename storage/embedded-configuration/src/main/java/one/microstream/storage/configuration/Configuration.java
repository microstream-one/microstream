
package one.microstream.storage.configuration;

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

import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;

import one.microstream.chars.XChars;
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfiguration;
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfigurationBuilder;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.StorageChannelCountProvider;
import one.microstream.storage.types.StorageDataFileEvaluator;
import one.microstream.storage.types.StorageEntityCacheEvaluator;
import one.microstream.storage.types.StorageFileNameProvider;
import one.microstream.storage.types.StorageHousekeepingController;
import one.microstream.storage.types.StorageLiveFileProvider;

/**
 * 
 * @deprecated replaced by {@link EmbeddedStorageConfigurationBuilder}, will be removed in a future release
 * @see EmbeddedStorageConfiguration
 * @see one.microstream.storage.configuration
 */
@Deprecated
public interface Configuration
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
	 * @see #Load()
	 *
	 * @return "microstream-storage.properties"
	 */
	public static String DefaultResourceName()
	{
		return "microstream-storage.properties";
	}

	/**
	 * Tries to load the default configuration properties file.
	 * <p>
	 * The search order is as follows:
	 * <ul>
	 * <li>The path set in the system property "microstream.storage.configuration.path"</li>
	 * <li>The file named "microstream-storage.properties" in
	 * <ul>
	 * <li>The classpath</li>
	 * <li>The application's directory</li>
	 * <li>The user home directory</li>
	 * </ul></li>
	 * </ul>
	 *
	 * @see #PathProperty()
	 * @see #DefaultResourceName()
	 *
	 * @return the loaded configuration or <code>null</code> if none was found
	 * 
	 * @deprecated replaced by {@link EmbeddedStorageConfiguration#load()}
	 */
	@Deprecated
	public static Configuration Load()
	{
		return Load(ConfigurationLoader.Defaults.defaultCharset());
	}

	/**
	 * Tries to load the default configuration properties file.
	 * <p>
	 * The search order is as follows:
	 * <ul>
	 * <li>The path set in the system property "microstream.storage.configuration.path"</li>
	 * <li>The file named "microstream-storage.properties" in
	 * <ul>
	 * <li>The classpath</li>
	 * <li>The application's directory</li>
	 * <li>The user home directory</li>
	 * </ul></li>
	 * </ul>
	 *
	 * @see #PathProperty()
	 * @see #DefaultResourceName()
	 *
	 * @param charset the charset used to load the configuration
	 * @return the loaded configuration or <code>null</code> if none was found
	 * 
	 * @deprecated replaced by {@link EmbeddedStorageConfiguration#load(Charset)}
	 */
	@Deprecated
	public static Configuration Load(
		final Charset charset
	)
	{
		final String path = System.getProperty(PathProperty());
		if(!XChars.isEmpty(path))
		{
			final Configuration configuration = Load(path, charset);
			if(configuration != null)
			{
				return configuration;
			}
		}

		final String      defaultName        = DefaultResourceName();
		final ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();
		final URL         url                = contextClassloader != null
			? contextClassloader.getResource(defaultName)
			: Configuration.class.getResource("/" + defaultName)
		;
		if(url != null)
		{
			return LoadIni(url, charset);
		}

		File file = new File(defaultName);
		if(file.exists())
		{
			return LoadIni(file, charset);
		}
		file = new File(System.getProperty("user.home"), defaultName);
		if(file.exists())
		{
			return LoadIni(file, charset);
		}

		return null;
	}

	/**
	 * Tries to load the configuration file from <code>path</code>.
	 * Depending on the file suffix either the XML or the INI loader is used.
	 * <p>
	 * The load order is as follows:
	 * <ul>
	 * <li>The classpath</li>
	 * <li>As an URL</li>
	 * <li>As a file</li>
	 * </ul>
	 *
	 * @param path a classpath resource, a file path or an URL
	 * @return the configuration or <code>null</code> if none was found
	 * 
	 * @deprecated replaced by {@link EmbeddedStorageConfiguration#load(String)}
	 */
	@Deprecated
	public static Configuration Load(
		final String path
	)
	{
		return Load(path, ConfigurationLoader.Defaults.defaultCharset());
	}

	/**
	 * Tries to load the configuration file from <code>path</code>.
	 * Depending on the file suffix either the XML or the INI loader is used.
	 * <p>
	 * The load order is as follows:
	 * <ul>
	 * <li>The classpath</li>
	 * <li>As an URL</li>
	 * <li>As a file</li>
	 * </ul>
	 *
	 * @param path a classpath resource, a file path or an URL
	 * @param charset the charset used to load the configuration
	 * @return the configuration or <code>null</code> if none was found
	 * 
	 * @deprecated replaced by {@link EmbeddedStorageConfiguration#load(String, Charset)}
	 */
	@Deprecated
	public static Configuration Load(
		final String  path   ,
		final Charset charset
	)
	{
		return path.toLowerCase().endsWith(".xml")
			? LoadXml(path, charset)
			: LoadIni(path, charset)
		;
	}

	/**
	 * Tries to load the configuration INI file from <code>path</code>.
	 * <p>
	 * The load order is as follows:
	 * <ul>
	 * <li>The classpath</li>
	 * <li>As an URL</li>
	 * <li>As a file</li>
	 * </ul>
	 *
	 * @param path a classpath resource, a file path or an URL
	 * @return the configuration or <code>null</code> if none was found
	 */
	public static Configuration LoadIni(
		final String path
	)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.load(path)
		);
	}

	/**
	 * Tries to load the configuration INI file from <code>path</code>.
	 * <p>
	 * The load order is as follows:
	 * <ul>
	 * <li>The classpath</li>
	 * <li>As an URL</li>
	 * <li>As a file</li>
	 * </ul>
	 *
	 * @param path a classpath resource, a file path or an URL
	 * @param charset the charset used to load the configuration
	 * @return the configuration or <code>null</code> if none was found
	 */
	public static Configuration LoadIni(
		final String  path   ,
		final Charset charset
	)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.load(path, charset)
		);
	}

	/**
	 * Tries to load the configuration INI file from <code>path</code>.
	 *
	 * @param path file system path
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadIni(
		final Path path
	)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.loadFromPath(path)
		);
	}

	/**
	 * Tries to load the configuration INI file from <code>path</code>.
	 *
	 * @param path file system path
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadIni(
		final Path    path   ,
		final Charset charset
	)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.loadFromPath(path, charset)
		);
	}

	/**
	 * Tries to load the configuration INI from the file <code>file</code>.
	 *
	 * @param file file path
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadIni(
		final File file
	)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.loadFromFile(file)
		);
	}

	/**
	 * Tries to load the configuration INI from the file <code>file</code>.
	 *
	 * @param file file path
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadIni(
		final File    file   ,
		final Charset charset
	)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.loadFromFile(file, charset)
		);
	}

	/**
	 * Tries to load the configuration INI from the URL <code>url</code>.
	 *
	 * @param url URL path
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadIni(
		final URL url
	)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.loadFromUrl(url)
		);
	}

	/**
	 * Tries to load the configuration INI from the URL <code>url</code>.
	 *
	 * @param url URL path
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadIni(
		final URL     url    ,
		final Charset charset
	)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.loadFromUrl(url, charset)
		);
	}

	/**
	 * Tries to load the configuration INI from the {@link InputStream} <code>inputStream</code>.
	 * <p>
	 * Note that the given <code>inputStream</code> will not be closed by this method.
	 *
	 * @param inputStream the stream to read from
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadIni(
		final InputStream inputStream
	)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.FromInputStream(inputStream).loadConfiguration()
		);
	}

	/**
	 * Tries to load the configuration INI from the {@link InputStream} <code>inputStream</code>.
	 * <p>
	 * Note that the given <code>inputStream</code> will not be closed by this method.
	 *
	 * @param inputStream the stream to read from
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadIni(
		final InputStream inputStream,
		final Charset     charset
	)
	{
		return ConfigurationParser.Ini().parse(
			ConfigurationLoader.FromInputStream(inputStream, charset).loadConfiguration()
		);
	}

	/**
	 * Tries to load the configuration XML file from <code>path</code>.
	 * <p>
	 * The load order is as follows:
	 * <ul>
	 * <li>The classpath</li>
	 * <li>As an URL</li>
	 * <li>As a file</li>
	 * </ul>
	 *
	 * @param path a classpath resource, a file path or an URL
	 * @return the configuration or <code>null</code> if none was found
	 */
	public static Configuration LoadXml(
		final String path
	)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.load(path)
		);
	}

	/**
	 * Tries to load the configuration XML file from <code>path</code>.
	 * <p>
	 * The load order is as follows:
	 * <ul>
	 * <li>The classpath</li>
	 * <li>As an URL</li>
	 * <li>As a file</li>
	 * </ul>
	 *
	 * @param path a classpath resource, a file path or an URL
	 * @param charset the charset used to load the configuration
	 * @return the configuration or <code>null</code> if none was found
	 */
	public static Configuration LoadXml(
		final String  path   ,
		final Charset charset
	)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.load(path, charset)
		);
	}

	/**
	 * Tries to load the configuration XML file from <code>path</code>.
	 *
	 * @param path file system path
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadXml(
		final Path path
	)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.loadFromPath(path)
		);
	}

	/**
	 * Tries to load the configuration XML file from <code>path</code>.
	 *
	 * @param path file system path
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadXml(
		final Path    path   ,
		final Charset charset
	)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.loadFromPath(path, charset)
		);
	}

	/**
	 * Tries to load the configuration XML from the file <code>file</code>.
	 *
	 * @param file file path
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadXml(
		final File file
	)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.loadFromFile(file)
		);
	}

	/**
	 * Tries to load the configuration XML from the file <code>file</code>.
	 *
	 * @param file file path
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadXml(
		final File    file   ,
		final Charset charset
	)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.loadFromFile(file, charset)
		);
	}

	/**
	 * Tries to load the configuration XML from the URL <code>url</code>.
	 *
	 * @param url URL path
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadXml(
		final URL url
	)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.loadFromUrl(url)
		);
	}

	/**
	 * Tries to load the configuration XML from the URL <code>url</code>.
	 *
	 * @param url URL path
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadXml(
		final URL     url    ,
		final Charset charset
	)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.loadFromUrl(url, charset)
		);
	}

	/**
	 * Tries to load the configuration XML from the {@link InputStream} <code>inputStream</code>.
	 * <p>
	 * Note that the given <code>inputStream</code> will not be closed by this method.
	 *
	 * @param inputStream the stream to read from
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadXml(
		final InputStream inputStream
	)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.FromInputStream(inputStream).loadConfiguration()
		);
	}

	/**
	 * Tries to load the configuration XML from the {@link InputStream} <code>inputStream</code>.
	 * <p>
	 * Note that the given <code>inputStream</code> will not be closed by this method.
	 *
	 * @param inputStream the stream to read from
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static Configuration LoadXml(
		final InputStream inputStream,
		final Charset     charset
	)
	{
		return ConfigurationParser.Xml().parse(
			ConfigurationLoader.FromInputStream(inputStream, charset).loadConfiguration()
		);
	}
	
	/**
	 * Exports this configuration as XML.
	 * <p>
	 * Note that the given <code>outputStream</code> will not be closed by this method.
	 *
	 * @param outputStream the outputStream to write to
	 * @since 3.1
	 */
	public default void exportXml(
		final OutputStream outputStream
	)
	{
		ConfigurationStorer.ToOutputStream(outputStream).storeConfiguration(
			ConfigurationAssembler.Xml().assemble(this)
		);
	}

	/**
	 * Exports this configuration as XML.
	 * <p>
	 * Note that the given <code>outputStream</code> will not be closed by this method.
	 *
	 * @param outputStream the outputStream to write to
	 * @param charset the charset used to export the configuration
	 * @since 3.1
	 */
	public default void exportXml(
		final OutputStream outputStream,
		final Charset      charset
	)
	{
		ConfigurationStorer.ToOutputStream(outputStream, charset).storeConfiguration(
			ConfigurationAssembler.Xml().assemble(this)
		);
	}

	/**
	 * Exports this configuration as a XML file to the specified path.
	 *
	 * @param path the path to write to
	 * @since 3.1
	 */
	public default void exportXml(
		final Path path
	)
	{
		ConfigurationStorer.storeToPath(
			path,
			ConfigurationAssembler.Xml().assemble(this)
		);
	}

	/**
	 * Exports this configuration as a XML file to the specified path.
	 *
	 * @param path the path to write to
	 * @param charset the charset used to export the configuration
	 * @since 3.1
	 */
	public default void exportXml(
		final Path    path   ,
		final Charset charset
	)
	{
		ConfigurationStorer.storeToPath(
			path,
			charset,
			ConfigurationAssembler.Xml().assemble(this)
		);
	}

	/**
	 * Exports this configuration as a XML file.
	 *
	 * @param file the file to write to
	 * @since 3.1
	 */
	public default void exportXml(
		final File file
	)
	{
		ConfigurationStorer.storeToFile(
			file,
			ConfigurationAssembler.Xml().assemble(this)
		);
	}

	/**
	 * Exports this configuration as a XML file.
	 *
	 * @param file the file to write to
	 * @param charset the charset used to export the configuration
	 * @since 3.1
	 */
	public default void exportXml(
		final File    file   ,
		final Charset charset
	)
	{
		ConfigurationStorer.storeToFile(
			file,
			charset,
			ConfigurationAssembler.Xml().assemble(this)
		);
	}

	/**
	 * Exports this configuration as a XML file.
	 *
	 * @param url the URL to write to
	 * @since 3.1
	 */
	public default void exportXml(
		final URL url
	)
	{
		ConfigurationStorer.storeToUrl(
			url,
			ConfigurationAssembler.Xml().assemble(this)
		);
	}

	/**
	 * Exports this configuration as a XML file.
	 *
	 * @param url the URL to write to
	 * @param charset the charset used to export the configuration
	 * @since 3.1
	 */
	public default void exportXml(
		final URL     url    ,
		final Charset charset
	)
	{
		ConfigurationStorer.storeToUrl(
			url,
			charset,
			ConfigurationAssembler.Xml().assemble(this)
		);
	}

	/**
	 * Exports this configuration as INI.
	 * <p>
	 * Note that the given <code>outputStream</code> will not be closed by this method.
	 *
	 * @param outputStream the outputStream to write to
	 * @since 3.1
	 */
	public default void exportIni(
		final OutputStream outputStream
	)
	{
		ConfigurationStorer.ToOutputStream(outputStream).storeConfiguration(
			ConfigurationAssembler.Ini().assemble(this)
		);
	}

	/**
	 * Exports this configuration as INI.
	 * <p>
	 * Note that the given <code>outputStream</code> will not be closed by this method.
	 *
	 * @param outputStream the outputStream to write to
	 * @param charset the charset used to export the configuration
	 * @since 3.1
	 */
	public default void exportIni(
		final OutputStream outputStream,
		final Charset      charset
	)
	{
		ConfigurationStorer.ToOutputStream(outputStream, charset).storeConfiguration(
			ConfigurationAssembler.Ini().assemble(this)
		);
	}

	/**
	 * Exports this configuration as an INI file to the specified path.
	 *
	 * @param path the path to write to
	 * @since 3.1
	 */
	public default void exportIni(
		final Path path
	)
	{
		ConfigurationStorer.storeToPath(
			path,
			ConfigurationAssembler.Ini().assemble(this)
		);
	}

	/**
	 * Exports this configuration as an INI file to the specified path.
	 *
	 * @param path the path to write to
	 * @param charset the charset used to export the configuration
	 * @since 3.1
	 */
	public default void exportIni(
		final Path    path   ,
		final Charset charset
	)
	{
		ConfigurationStorer.storeToPath(
			path,
			charset,
			ConfigurationAssembler.Ini().assemble(this)
		);
	}

	/**
	 * Exports this configuration as a INI file.
	 *
	 * @param file the file to write to
	 * @since 3.1
	 */
	public default void exportIni(
		final File file
	)
	{
		ConfigurationStorer.storeToFile(
			file,
			ConfigurationAssembler.Ini().assemble(this)
		);
	}

	/**
	 * Exports this configuration as a INI file.
	 *
	 * @param file the file to write to
	 * @param charset the charset used to export the configuration
	 * @since 3.1
	 */
	public default void exportIni(
		final File    file   ,
		final Charset charset
	)
	{
		ConfigurationStorer.storeToFile(
			file,
			charset,
			ConfigurationAssembler.Ini().assemble(this)
		);
	}

	/**
	 * Exports this configuration as a INI file.
	 *
	 * @param url the URL to write to
	 * @since 3.1
	 */
	public default void exportIni(
		final URL url
	)
	{
		ConfigurationStorer.storeToUrl(
			url,
			ConfigurationAssembler.Ini().assemble(this)
		);
	}

	/**
	 * Exports this configuration as a INI file.
	 *
	 * @param url the URL to write to
	 * @param charset the charset used to export the configuration
	 * @since 3.1
	 */
	public default void exportIni(
		final URL     url    ,
		final Charset charset
	)
	{
		ConfigurationStorer.storeToUrl(
			url,
			charset,
			ConfigurationAssembler.Ini().assemble(this)
		);
	}

	/**
	 * Creates an {@link EmbeddedStorageFoundation} based on the settings of this {@link Configuration}.
	 *
	 * @return an {@link EmbeddedStorageFoundation}
	 *
	 * @see EmbeddedStorageFoundationCreator
	 */
	public default EmbeddedStorageFoundation<?> createEmbeddedStorageFoundation()
	{
		return EmbeddedStorageFoundationCreator.New().createFoundation(this);
	}

	public Configuration setBaseDirectory(String baseDirectory);

	public default Configuration setBaseDirectoryInUserHome(final String baseDirectoryInUserHome)
	{
		final File userHomeDir = new File(System.getProperty("user.home"));
		this.setBaseDirectory(new File(userHomeDir, baseDirectoryInUserHome).getAbsolutePath());
		return this;
	}

	public String getBaseDirectory();

	public Configuration setDeletionDirectory(String deletionDirectory);

	public String getDeletionDirectory();

	public Configuration setTruncationDirectory(String truncationDirectory);

	public String getTruncationDirectory();

	public Configuration setBackupDirectory(String backupDirectory);

	public default Configuration setBackupDirectoryInUserHome(final String backupDirectoryInUserHome)
	{
		final File userHomeDir = new File(System.getProperty("user.home"));
		this.setBackupDirectory(new File(userHomeDir, backupDirectoryInUserHome).getAbsolutePath());
		return this;
	}

	public String getBackupDirectory();

	public Configuration setChannelCount(int channelCount);

	public int getChannelCount();

	public Configuration setChannelDirectoryPrefix(String channelDirectoryPrefix);

	public String getChannelDirectoryPrefix();

	public Configuration setDataFilePrefix(String dataFilePrefix);

	public String getDataFilePrefix();

	public Configuration setDataFileSuffix(String dataFileSuffix);

	public String getDataFileSuffix();

	public Configuration setTransactionFilePrefix(String transactionFilePrefix);

	public String getTransactionFilePrefix();

	public Configuration setTransactionFileSuffix(String transactionFileSuffix);

	public String getTransactionFileSuffix();

	public Configuration setTypeDictionaryFilename(String typeDictionaryFilename);

	public String getTypeDictionaryFilename();

	public Configuration setRescuedFileSuffix(String rescuedFileSuffix);

	public String getRescuedFileSuffix();

	public Configuration setLockFileName(String lockFileName);

	public String getLockFileName();

	public default Configuration setHouseKeepingInterval(final long houseKeepingInterval)
	{
		return this.setHousekeepingIntervalMs(houseKeepingInterval);
	}

	public Configuration setHousekeepingIntervalMs(long houseKeepingIntervalMs);

	public default long getHouseKeepingInterval()
	{
		return this.getHousekeepingIntervalMs();
	}

	public long getHousekeepingIntervalMs();

	public default Configuration setHouseKeepingNanoTimeBudget(final long houseKeepingNanoTimeBudget)
	{
		return this.setHousekeepingTimeBudgetNs(houseKeepingNanoTimeBudget);
	}

	public Configuration setHousekeepingTimeBudgetNs(long housekeepingTimeBudgetNs);

	public default long getHouseKeepingNanoTimeBudget()
	{
		return this.getHousekeepingTimeBudgetNs();
	}

	public long getHousekeepingTimeBudgetNs();

	public Configuration setEntityCacheThreshold(long entityCacheThreshold);

	public long getEntityCacheThreshold();

	@Deprecated
	public default Configuration setEntityCacheTimeout(final long entityCacheTimeout)
	{
		return this.setEntityCacheTimeoutMs(entityCacheTimeout);
	}

	public Configuration setEntityCacheTimeoutMs(long entityCacheTimeoutMs);

	@Deprecated
	public default long getEntityCacheTimeout()
	{
		return this.getEntityCacheTimeoutMs();
	}

	public long getEntityCacheTimeoutMs();

	@Deprecated
	public default Configuration setDataFileMinSize(final int dataFileMinSize)
	{
		return this.setDataFileMinimumSize(dataFileMinSize);
	}

	public Configuration setDataFileMinimumSize(int dataFileMinimumSize);

	@Deprecated
	public default int getDataFileMinSize()
	{
		return this.getDataFileMinimumSize();
	}

	public int getDataFileMinimumSize();

	@Deprecated
	public default Configuration setDataFileMaxSize(final int dataFileMaxSize)
	{
		return this.setDataFileMaximumSize(dataFileMaxSize);
	}

	public Configuration setDataFileMaximumSize(int dataFileMaximumSize);

	@Deprecated
	public default int getDataFileMaxSize()
	{
		return this.getDataFileMaximumSize();
	}

	public int getDataFileMaximumSize();

	@Deprecated
	public default Configuration setDataFileDissolveRatio(final double dataFileDissolveRatio)
	{
		return this.setDataFileMinimumUseRatio(dataFileDissolveRatio);
	}

	public Configuration setDataFileMinimumUseRatio(double dataFileMinimumUseRatio);

	@Deprecated
	public default double getDataFileDissolveRatio()
	{
		return this.getDataFileMinimumUseRatio();
	}

	public double getDataFileMinimumUseRatio();

	public Configuration setDataFileCleanupHeadFile(boolean dataFileCleanupHeadFile);

	public boolean getDataFileCleanupHeadFile();


	/**
	 * Creates a new {@link Configuration} with the default settings.
	 *
	 * @return a new {@link Configuration}
	 *
	 * @see StorageChannelCountProvider.Defaults
	 * @see StorageHousekeepingController.Defaults
	 * @see StorageEntityCacheEvaluator.Defaults
	 * @see StorageDataFileEvaluator.Defaults
	 */
	public static Configuration Default()
	{
		return new Configuration.Default();
	}


	public static class Default implements Configuration
	{
		private String  baseDirectory            = StorageLiveFileProvider.Defaults.defaultStorageDirectory();
		private String  deletionDirectory        = null;
		private String  truncationDirectory      = null;
		private String  backupDirectory          = null; // no on-the-fly backup by default
		private String  channelDirectoryPrefix   = StorageFileNameProvider.Defaults.defaultChannelDirectoryPrefix();
		private String  dataFilePrefix           = StorageFileNameProvider.Defaults.defaultDataFilePrefix();
		private String  dataFileSuffix           = StorageFileNameProvider.Defaults.defaultDataFileSuffix();
		private String  transactionFilePrefix    = StorageFileNameProvider.Defaults.defaultTransactionsFilePrefix();
		private String  transactionFileSuffix    = StorageFileNameProvider.Defaults.defaultTransactionsFileSuffix();
		private String  typeDictionaryFilename   = StorageFileNameProvider.Defaults.defaultTypeDictionaryFileName();
		private String  rescuedFileSuffix        = StorageFileNameProvider.Defaults.defaultRescuedFileSuffix();
		private String  lockFileName             = StorageFileNameProvider.Defaults.defaultLockFileName();

		private int     channelCount             = StorageChannelCountProvider.Defaults.defaultChannelCount();

		private long    housekeepingIntervalMs   = StorageHousekeepingController.Defaults.defaultHousekeepingIntervalMs();
		private long    housekeepingTimeBudgetNs = StorageHousekeepingController.Defaults.defaultHousekeepingTimeBudgetNs();

		private long    entityCacheTimeoutMs     = StorageEntityCacheEvaluator.Defaults.defaultTimeoutMs();
		private long    entityCacheThreshold     = StorageEntityCacheEvaluator.Defaults.defaultCacheThreshold();

		private int     dataFileMinimumSize      = StorageDataFileEvaluator.Defaults.defaultFileMinimumSize();
		private int     dataFileMaximumSize      = StorageDataFileEvaluator.Defaults.defaultFileMaximumSize();
		private double  dataFileMinimumUseRatio  = StorageDataFileEvaluator.Defaults.defaultMinimumUseRatio();
		private boolean dataFileCleanupHeadFile  = StorageDataFileEvaluator.Defaults.defaultResolveHeadfile();


		Default()
		{
			super();
		}

		@Override
		public Configuration setBaseDirectory(
			final String baseDirectory
		)
		{
			this.baseDirectory = notEmpty(baseDirectory);
			return this;
		}

		@Override
		public String getBaseDirectory()
		{
			return this.baseDirectory;
		}

		@Override
		public Configuration setDeletionDirectory(
			final String deletionDirectory
		)
		{
			this.deletionDirectory = deletionDirectory;
			return this;
		}

		@Override
		public String getDeletionDirectory()
		{
			return this.deletionDirectory;
		}

		@Override
		public Configuration setTruncationDirectory(
			final String truncationDirectory
		)
		{
			this.truncationDirectory = truncationDirectory;
			return this;
		}

		@Override
		public String getTruncationDirectory()
		{
			return this.truncationDirectory;
		}

		@Override
		public Configuration setBackupDirectory(
			final String backupDirectory
		)
		{
			this.backupDirectory = backupDirectory;
			return this;
		}

		@Override
		public String getBackupDirectory()
		{
			return this.backupDirectory;
		}

		@Override
		public Configuration setChannelCount(
			final int channelCount
		)
		{
			StorageChannelCountProvider.Validation.validateParameters(channelCount);
			this.channelCount = channelCount;
			return this;
		}

		@Override
		public int getChannelCount()
		{
			return this.channelCount;
		}

		@Override
		public Configuration setChannelDirectoryPrefix(
			final String channelDirectoryPrefix
		)
		{
			this.channelDirectoryPrefix = notNull(channelDirectoryPrefix);
			return this;
		}

		@Override
		public String getChannelDirectoryPrefix()
		{
			return this.channelDirectoryPrefix;
		}

		@Override
		public Configuration setDataFilePrefix(
			final String dataFilePrefix
		)
		{
			this.dataFilePrefix = notNull(dataFilePrefix);
			return this;
		}

		@Override
		public String getDataFilePrefix()
		{
			return this.dataFilePrefix;
		}

		@Override
		public Configuration setDataFileSuffix(
			final String dataFileSuffix
		)
		{
			this.dataFileSuffix = notNull(dataFileSuffix);
			return this;
		}

		@Override
		public String getDataFileSuffix()
		{
			return this.dataFileSuffix;
		}

		@Override
		public Configuration setTransactionFilePrefix(
			final String transactionFilePrefix
		)
		{
			this.transactionFilePrefix = notNull(transactionFilePrefix);
			return this;
		}

		@Override
		public String getTransactionFilePrefix()
		{
			return this.transactionFilePrefix;
		}

		@Override
		public Configuration setTransactionFileSuffix(
			final String transactionFileSuffix
		)
		{
			this.transactionFileSuffix = notNull(transactionFileSuffix);
			return this;
		}

		@Override
		public String getTransactionFileSuffix()
		{
			return this.transactionFileSuffix;
		}

		@Override
		public Configuration setTypeDictionaryFilename(
			final String typeDictionaryFilename
		)
		{
			this.typeDictionaryFilename = notEmpty(typeDictionaryFilename);
			return this;
		}

		@Override
		public String getTypeDictionaryFilename()
		{
			return this.typeDictionaryFilename;
		}

		@Override
		public Configuration setRescuedFileSuffix(
			final String rescuedFileSuffix
		)
		{
			this.rescuedFileSuffix = notEmpty(rescuedFileSuffix);
			return this;
		}

		@Override
		public String getRescuedFileSuffix()
		{
			return this.rescuedFileSuffix;
		}

		@Override
		public Configuration setLockFileName(
			final String lockFileName
		)
		{
			this.lockFileName = lockFileName;
			return this;
		}

		@Override
		public String getLockFileName()
		{
			return this.lockFileName;
		}

		@Override
		public Configuration setHousekeepingIntervalMs(
			final long housekeepingIntervalMs
		)
		{
			StorageHousekeepingController.Validation.validateParameters(
				housekeepingIntervalMs,
				StorageHousekeepingController.Defaults.defaultHousekeepingTimeBudgetNs()
			);
			this.housekeepingIntervalMs = housekeepingIntervalMs;
			return this;
		}

		@Override
		public long getHousekeepingIntervalMs()
		{
			return this.housekeepingIntervalMs;
		}

		@Override
		public Configuration setHousekeepingTimeBudgetNs(
			final long housekeepingNanoTimeBudgetNs
		)
		{
			StorageHousekeepingController.Validation.validateParameters(
				StorageHousekeepingController.Defaults.defaultHousekeepingIntervalMs(),
				housekeepingNanoTimeBudgetNs
			);
			this.housekeepingTimeBudgetNs = housekeepingNanoTimeBudgetNs;
			return this;
		}

		@Override
		public long getHousekeepingTimeBudgetNs()
		{
			return this.housekeepingTimeBudgetNs;
		}

		@Override
		public Configuration setEntityCacheThreshold(
			final long entityCacheThreshold
		)
		{
			StorageEntityCacheEvaluator.Validation.validateParameters(
				StorageEntityCacheEvaluator.Defaults.defaultTimeoutMs(),
				entityCacheThreshold
			);
			this.entityCacheThreshold = entityCacheThreshold;
			return this;
		}

		@Override
		public long getEntityCacheThreshold()
		{
			return this.entityCacheThreshold;
		}

		@Override
		public Configuration setEntityCacheTimeoutMs(
			final long entityCacheTimeoutMs
		)
		{
			StorageEntityCacheEvaluator.Validation.validateParameters(
				entityCacheTimeoutMs,
				StorageEntityCacheEvaluator.Defaults.defaultCacheThreshold()
			);
			this.entityCacheTimeoutMs = entityCacheTimeoutMs;
			return this;
		}

		@Override
		public long getEntityCacheTimeoutMs()
		{
			return this.entityCacheTimeoutMs;
		}

		@Override
		public Configuration setDataFileMinimumSize(
			final int dataFileMinimumSize
		)
		{
			StorageDataFileEvaluator.Validation.validateParameters(
				dataFileMinimumSize,
				StorageDataFileEvaluator.Validation.maximumFileSize(),
				StorageDataFileEvaluator.Defaults.defaultMinimumUseRatio()
			);
			this.dataFileMinimumSize = dataFileMinimumSize;
			return this;
		}

		@Override
		public int getDataFileMinimumSize()
		{
			return this.dataFileMinimumSize;
		}

		@Override
		public Configuration setDataFileMaximumSize(
			final int dataFileMaximumSize
		)
		{
			StorageDataFileEvaluator.Validation.validateParameters(
				StorageDataFileEvaluator.Validation.minimumFileSize(),
				dataFileMaximumSize,
				StorageDataFileEvaluator.Defaults.defaultMinimumUseRatio()
			);
			this.dataFileMaximumSize = dataFileMaximumSize;
			return this;
		}

		@Override
		public int getDataFileMaximumSize()
		{
			return this.dataFileMaximumSize;
		}

		@Override
		public Configuration setDataFileMinimumUseRatio(
			final double dataFileMinimumUseRatio
		)
		{
			StorageDataFileEvaluator.Validation.validateParameters(
				StorageDataFileEvaluator.Defaults.defaultFileMinimumSize(),
				StorageDataFileEvaluator.Defaults.defaultFileMaximumSize(),
				dataFileMinimumUseRatio
			);
			this.dataFileMinimumUseRatio = dataFileMinimumUseRatio;
			return this;
		}

		@Override
		public double getDataFileMinimumUseRatio()
		{
			return this.dataFileMinimumUseRatio;
		}

		@Override
		public Configuration setDataFileCleanupHeadFile(
			final boolean dataFileCleanupHeadFile
		)
		{
			this.dataFileCleanupHeadFile = dataFileCleanupHeadFile;
			return this;
		}

		@Override
		public boolean getDataFileCleanupHeadFile()
		{
			return this.dataFileCleanupHeadFile;
		}

	}

}
