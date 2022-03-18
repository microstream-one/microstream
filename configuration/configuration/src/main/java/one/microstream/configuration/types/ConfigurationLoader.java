
package one.microstream.configuration.types;

/*-
 * #%L
 * microstream-configuration
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;

import one.microstream.chars.XChars;
import one.microstream.configuration.exceptions.ConfigurationException;
import one.microstream.configuration.exceptions.ConfigurationExceptionNoConfigurationFound;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.util.logging.Logging;

/**
 * Loader for external configuration resources.
 * <p>
 * Supported resource types:
 * <ul>
 * <li>{@link Path}</li>
 * <li>{@link File}</li>
 * <li>{@link URL}</li>
 * <li>{@link InputStream}</li>
 * </ul>
 * 
 * @see Configuration.Builder#load(ConfigurationLoader, ConfigurationParser)
 */
@FunctionalInterface
public interface ConfigurationLoader
{
	/**
	 * Loads the configuration from the given resource.
	 * 
	 * @return the configuration resource's content.
	 * @throws ConfigurationException if an error occurs while loading the resource
	 */
	public String loadConfiguration();
	
	
	/**
	 * Loads the configuration from the given resource.
	 * <p>
	 * The load order is as follows:
	 * <ul>
	 * <li>The classpath</li>
	 * <li>As an URL</li>
	 * <li>As a file</li>
	 * </ul>
	 * 
	 * @param path a classpath resource, a file path or an URL
	 * @return the configuration resource's content.
	 * @throws ConfigurationExceptionNoConfigurationFound if no configuration can be found at the given path
	 */
	public static ConfigurationLoader New(
		final String path
	)
	{
		return New(
			path                     ,
			Defaults.defaultCharset()
		);
	}
	
	/**
	 * Loads the configuration from the given resource.
	 * <p>
	 * The load order is as follows:
	 * <ul>
	 * <li>The classpath</li>
	 * <li>As an URL</li>
	 * <li>As a file</li>
	 * </ul>
	 * 
	 * @return the configuration resource's content.
	 * @param path a classpath resource, a file path or an URL
	 * @param charset the charset used to load the configuration
	 * @throws ConfigurationExceptionNoConfigurationFound if no configuration can be found at the given path
	 */
	public static ConfigurationLoader New(
		final String  path   ,
		final Charset charset
	)
	{
		final ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();
	    final URL         url                = contextClassloader != null
			? contextClassloader.getResource(path)
			: ConfigurationLoader.class.getResource(path)
		;
		if(url != null)
		{
			return New(url, charset);
		}
			
		try
		{
			return New(new URL(path), charset);
		}
		catch(final MalformedURLException e)
		{
			final File file = new File(path);
			if(file.exists())
			{
				return New(file, charset);
			}
		}
		
		throw new ConfigurationExceptionNoConfigurationFound("No configuration found at: " + path);
	}
	
	/**
	 * Tries to load the configuration from <code>path</code>.
	 * 
	 * @param path file system path
	 * @return the configuration
	 * @throws ConfigurationException if the configuration couldn't be loaded
	 */
	public static ConfigurationLoader New(
		final Path path
	)
	{
		return New(
			path                     ,
			Defaults.defaultCharset()
		);
	}

	
	/**
	 * Tries to load the configuration from <code>path</code>.
	 * 
	 * @param path file system path
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws ConfigurationException if the configuration couldn't be loaded
	 */
	public static ConfigurationLoader New(
		final Path    path   ,
		final Charset charset
	)
	{
		return new ConfigurationLoader.PathLoader(
			notNull(path)   ,
			notNull(charset)
		);
	}
	
	/**
	 * Tries to load the configuration from <code>file</code>.
	 * 
	 * @param file file path
	 * @return the configuration
	 * @throws ConfigurationException if the configuration couldn't be loaded
	 */
	public static ConfigurationLoader New(
		final File file
	)
	{
		return New(
			file                     ,
			Defaults.defaultCharset()
		);
	}
	
	/**
	 * Tries to load the configuration from <code>file</code>.
	 * 
	 * @param file file path
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws ConfigurationException if the configuration couldn't be loaded
	 */
	public static ConfigurationLoader New(
		final File    file   ,
		final Charset charset
	)
	{
		return new ConfigurationLoader.FileLoader(
			notNull(file)   ,
			notNull(charset)
		);
	}
	
	/**
	 * Tries to load the configuration from the URL <code>url</code>.
	 * 
	 * @param url URL path
	 * @return the configuration
	 * @throws ConfigurationException if the configuration couldn't be loaded
	 */
	public static ConfigurationLoader New(
		final URL url
	)
	{
		return New(
			url                      ,
			Defaults.defaultCharset()
		);
	}
	
	/**
	 * Tries to load the configuration from the URL <code>url</code>.
	 * 
	 * @param url URL path
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws ConfigurationException if the configuration couldn't be loaded
	 */
	public static ConfigurationLoader New(
		final URL     url    ,
		final Charset charset
	)
	{
		return new ConfigurationLoader.UrlLoader(
			notNull(url)    ,
			notNull(charset)
		);
	}

	/**
	 * Tries to load the configuration from the {@link InputStream} <code>inputStream</code>.
	 * 
	 * @param inputStream the stream to read from
	 * @return the configuration
	 * @throws ConfigurationException if the configuration couldn't be loaded
	 */
	public static ConfigurationLoader New(
		final InputStream inputStream
	)
	{
		return New(
			inputStream              ,
			Defaults.defaultCharset()
		);
	}
	
	/**
	 * Tries to load the configuration from the {@link InputStream} <code>inputStream</code>.
	 * 
	 * @param inputStream the stream to read from
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws ConfigurationException if the configuration couldn't be loaded
	 */
	public static ConfigurationLoader New(
		final InputStream inputStream,
		final Charset     charset
	)
	{
		return new ConfigurationLoader.InputStreamLoader(
			notNull(inputStream),
			notNull(charset)
		);
	}
	
	
	public static interface Defaults
	{
		public static Charset defaultCharset()
		{
			return StandardCharsets.UTF_8;
		}
	}
	
	
	public static class InputStreamLoader implements ConfigurationLoader
	{
		private final static Logger logger = Logging.getLogger(InputStreamLoader.class);
		
		private final InputStream inputStream;
		private final Charset     charset    ;
		
		InputStreamLoader(
			final InputStream inputStream,
			final Charset     charset
		)
		{
			super();
			this.inputStream = inputStream;
			this.charset     = charset    ;
		}
		
		@Override
		public String loadConfiguration()
		{
			logger.info(
				"Loading configuration: {} ({})",
				this.inputStream,
				this.charset.displayName()
			);
			
			try
			{
				return XChars.readStringFromInputStream(this.inputStream, this.charset);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
	}
	
	public static class UrlLoader implements ConfigurationLoader
	{
		private final static Logger logger = Logging.getLogger(UrlLoader.class);
		
		private final URL     url    ;
		private final Charset charset;
		
		UrlLoader(
			final URL     url    ,
			final Charset charset
		)
		{
			super();
			this.url     = url    ;
			this.charset = charset;
		}
		
		@Override
		public String loadConfiguration()
		{
			logger.info(
				"Loading configuration: {} ({})",
				this.url.toExternalForm(),
				this.charset.displayName()
			);
			
			try(InputStream in = this.url.openStream())
			{
				return XChars.readStringFromInputStream(in, this.charset);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
	}
	
	public static class PathLoader implements ConfigurationLoader
	{
		private final static Logger logger = Logging.getLogger(PathLoader.class);
		
		private final Path    path   ;
		private final Charset charset;
		
		PathLoader(
			final Path    path   ,
			final Charset charset
		)
		{
			super();
			this.path    = path   ;
			this.charset = charset;
		}
		
		@Override
		public String loadConfiguration()
		{
			logger.info(
				"Loading configuration: {} ({})",
				this.path.toAbsolutePath().toString(),
				this.charset.displayName()
			);
			
			try(InputStream in = Files.newInputStream(this.path))
			{
				return XChars.readStringFromInputStream(in, this.charset);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
	}
	
	public static class FileLoader implements ConfigurationLoader
	{
		private final static Logger logger = Logging.getLogger(FileLoader.class);
		
		private final File    file   ;
		private final Charset charset;
		
		FileLoader(
			final File    file   ,
			final Charset charset
		)
		{
			super();
			this.file    = file   ;
			this.charset = charset;
		}
		
		@Override
		public String loadConfiguration()
		{
			logger.info(
				"Loading configuration: {} ({})",
				this.file.getAbsolutePath(),
				this.charset.displayName()
			);
			
			try(InputStream in = new FileInputStream(this.file))
			{
				return XChars.readStringFromInputStream(in, this.charset);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
	}
	
}
