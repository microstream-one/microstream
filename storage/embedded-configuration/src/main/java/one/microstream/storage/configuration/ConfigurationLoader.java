
package one.microstream.storage.configuration;

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

import one.microstream.chars.XChars;

/**
 * 
 * @deprecated replaced by generic {@link one.microstream.configuration.types.ConfigurationLoader}, will be removed in a future release
 * @see one.microstream.storage.configuration
 */
@Deprecated
@FunctionalInterface
public interface ConfigurationLoader
{
	/**
	 * Loads the configuration from the given resource.
	 * 
	 * @return the configuration resource's content.
	 * @throws StorageConfigurationException if an error occurs while loading the resource
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
	 * @throws StorageConfigurationException if an error occurs while loading the resource
	 */
	public static String load(
		final String path
	)
	{
		final ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();
	    final URL         url                = contextClassloader != null
			? contextClassloader.getResource(path)
			: ConfigurationLoader.class.getResource(path)
		;
		if(url != null)
		{
			return loadFromUrl(url);
		}
			
		try
		{
			return loadFromUrl(new URL(path));
		}
		catch(final MalformedURLException e)
		{
			final File file = new File(path);
			if(file.exists())
			{
				return loadFromFile(file);
			}
		}
		
		return null;
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
	 * @throws StorageConfigurationException if an error occurs while loading the resource
	 */
	public static String load(
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
			return loadFromUrl(url, charset);
		}
			
		try
		{
			return loadFromUrl(new URL(path), charset);
		}
		catch(final MalformedURLException e)
		{
			final File file = new File(path);
			if(file.exists())
			{
				return loadFromFile(file, charset);
			}
		}
		
		return null;
	}
	
	/**
	 * Tries to load the configuration from <code>path</code>.
	 * 
	 * @param path file system path
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static String loadFromPath(
		final Path path
	)
	{
		return loadFromPath(path, Defaults.defaultCharset());
	}

	
	/**
	 * Tries to load the configuration from <code>path</code>.
	 * 
	 * @param path file system path
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static String loadFromPath(
		final Path    path   ,
		final Charset charset
	)
	{
		try(InputStream in = Files.newInputStream(path))
		{
			return FromInputStream(in, charset).loadConfiguration();
		}
		catch(final IOException e)
		{
			throw new StorageConfigurationNotFoundException(e);
		}
	}
	
	/**
	 * Tries to load the configuration from <code>file</code>.
	 * 
	 * @param file file path
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static String loadFromFile(
		final File file
	)
	{
		return loadFromFile(file, Defaults.defaultCharset());
	}
	
	/**
	 * Tries to load the configuration from <code>file</code>.
	 * 
	 * @param file file path
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static String loadFromFile(
		final File    file   ,
		final Charset charset
	)
	{
		try(InputStream in = new FileInputStream(file))
		{
			return FromInputStream(in, charset).loadConfiguration();
		}
		catch(final IOException e)
		{
			throw new StorageConfigurationNotFoundException(e);
		}
	}
	
	/**
	 * Tries to load the configuration from the URL <code>url</code>.
	 * 
	 * @param url URL path
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static String loadFromUrl(
		final URL url
	)
	{
		return loadFromUrl(url, Defaults.defaultCharset());
	}
	
	/**
	 * Tries to load the configuration from the URL <code>url</code>.
	 * 
	 * @param url URL path
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static String loadFromUrl(
		final URL     url    ,
		final Charset charset
	)
	{
		try(InputStream in = url.openStream())
		{
			return FromInputStream(in, charset).loadConfiguration();
		}
		catch(final IOException e)
		{
			throw new StorageConfigurationNotFoundException(e);
		}
	}

	/**
	 * Tries to load the configuration from the {@link InputStream} <code>inputStream</code>.
	 * 
	 * @param inputStream the stream to read from
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static ConfigurationLoader FromInputStream(
		final InputStream inputStream
	)
	{
		return FromInputStream(inputStream, Defaults.defaultCharset());
	}
	
	/**
	 * Tries to load the configuration from the {@link InputStream} <code>inputStream</code>.
	 * 
	 * @param inputStream the stream to read from
	 * @param charset the charset used to load the configuration
	 * @return the configuration
	 * @throws StorageConfigurationException if the configuration couldn't be loaded
	 */
	public static ConfigurationLoader FromInputStream(
		final InputStream inputStream,
		final Charset     charset
	)
	{
		return new InputStreamConfigurationLoader(
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
	
	
	public static class InputStreamConfigurationLoader implements ConfigurationLoader
	{
		private final InputStream inputStream;
		private final Charset     charset    ;
		
		InputStreamConfigurationLoader(
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
			try
			{
				return XChars.readStringFromInputStream(this.inputStream, this.charset);
			}
			catch(final IOException e)
			{
				throw new StorageConfigurationIoException(e);
			}
		}
		
	}
	
}
