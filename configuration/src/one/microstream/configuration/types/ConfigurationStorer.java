
package one.microstream.configuration.types;

import static one.microstream.X.notNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import one.microstream.exceptions.IORuntimeException;

/**
 * Storer which exports configuration data to external targets.
 *
 * @see Configuration#store(ConfigurationStorer, ConfigurationAssembler)
 */
@FunctionalInterface
public interface ConfigurationStorer
{
	/**
	 * Stores the configuration data to an external target.
	 * 
	 * @param configurationData the data to export
	 */
	public void storeConfiguration(String configurationData);
	

	/**
	 * Pseudo-constructor method to create a new {@link ConfigurationStorer}.
	 * 
	 * @param path the target path
	 * @return a new {@link ConfigurationStorer}
	 * @see #New(Path, Charset)
	 * @see Defaults#defaultCharset()
	 */
	public static ConfigurationStorer New(
		final Path path
	)
	{
		return New(
			path                     ,
			Defaults.defaultCharset()
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link ConfigurationStorer}.
	 * 
	 * @param path the target path
	 * @param charset the charset to use to convert the configuration data into binary form
	 * @return a new {@link ConfigurationStorer}
	 */
	public static ConfigurationStorer New(
		final Path    path   ,
		final Charset charset
	)
	{
		return new ConfigurationStorer.PathStorer(
			notNull(path)   ,
			notNull(charset)
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link ConfigurationStorer}.
	 * 
	 * @param file the target file
	 * @return a new {@link ConfigurationStorer}
	 * @see #New(File, Charset)
	 * @see Defaults#defaultCharset()
	 */
	public static ConfigurationStorer New(
		final File file
	)
	{
		return New(
			file                     ,
			Defaults.defaultCharset()
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link ConfigurationStorer}.
	 * 
	 * @param file the target file
	 * @param charset the charset to use to convert the configuration data into binary form
	 * @return a new {@link ConfigurationStorer}
	 */
	public static ConfigurationStorer New(
		final File    file   ,
		final Charset charset
	)
	{
		return new ConfigurationStorer.FileStorer(
			notNull(file)   ,
			notNull(charset)
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link ConfigurationStorer}.
	 * 
	 * @param url the target url
	 * @return a new {@link ConfigurationStorer}
	 * @see #New(URL, Charset)
	 * @see Defaults#defaultCharset()
	 */
	public static ConfigurationStorer New(
		final URL url
	)
	{
		return New(
			url                      ,
			Defaults.defaultCharset()
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link ConfigurationStorer}.
	 * 
	 * @param url the target url
	 * @param charset the charset to use to convert the configuration data into binary form
	 * @return a new {@link ConfigurationStorer}
	 */
	public static ConfigurationStorer New(
		final URL     url    ,
		final Charset charset
	)
	{
		return new ConfigurationStorer.UrlStorer(
			notNull(url)    ,
			notNull(charset)
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link ConfigurationStorer}.
	 * 
	 * @param outputStream the target output stream
	 * @return a new {@link ConfigurationStorer}
	 * @see #New(OutputStream, Charset)
	 * @see Defaults#defaultCharset()
	 */
	public static ConfigurationStorer New(
		final OutputStream outputStream
	)
	{
		return New(
			outputStream              ,
			Defaults.defaultCharset()
		);
	}

	/**
	 * Pseudo-constructor method to create a new {@link ConfigurationStorer}.
	 * 
	 * @param outputStream the target output stream
	 * @param charset the charset to use to convert the configuration data into binary form
	 * @return a new {@link ConfigurationStorer}
	 */
	public static ConfigurationStorer New(
		final OutputStream outputStream,
		final Charset      charset
	)
	{
		return new ConfigurationStorer.OutputStreamStorer(
			notNull(outputStream),
			notNull(charset)
		);
	}
	
	
	public static interface Defaults
	{
		/**
		 * @return the default charset (UTF-8) which is used to convert the configuration data into binary form
		 */
		public static Charset defaultCharset()
		{
			return StandardCharsets.UTF_8;
		}
	}
	
	
	public static class OutputStreamStorer implements ConfigurationStorer
	{
		private final OutputStream outputStream;
		private final Charset      charset    ;
		
		OutputStreamStorer(
			final OutputStream outputStream,
			final Charset      charset
		)
		{
			super();
			this.outputStream = outputStream;
			this.charset      = charset     ;
		}
		
		@Override
		public void storeConfiguration(final String configurationData)
		{
			try(OutputStreamWriter writer = new OutputStreamWriter(this.outputStream, this.charset))
			{
				writer.write(configurationData);
				writer.flush();
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
	}
	
	public static class UrlStorer implements ConfigurationStorer
	{
		private final URL     url    ;
		private final Charset charset;
		
		UrlStorer(
			final URL     url    ,
			final Charset charset
		)
		{
			super();
			this.url     = url    ;
			this.charset = charset;
		}
		
		@Override
		public void storeConfiguration(final String configurationData)
		{
			try
			{
				final URLConnection urlConnection = this.url.openConnection();
				urlConnection.setDoOutput(true);
				try(OutputStreamWriter writer = new OutputStreamWriter(
					urlConnection.getOutputStream(),
					this.charset
				))
				{
					writer.write(configurationData);
					writer.flush();
				}
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
	}
	
	public static class PathStorer implements ConfigurationStorer
	{
		private final Path    path   ;
		private final Charset charset;
		
		PathStorer(
			final Path    path   ,
			final Charset charset
		)
		{
			super();
			this.path    = path   ;
			this.charset = charset;
		}
		
		@Override
		public void storeConfiguration(final String configurationData)
		{
			try(OutputStreamWriter writer = new OutputStreamWriter(
				Files.newOutputStream(this.path),
				this.charset
			))
			{
				writer.write(configurationData);
				writer.flush();
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
	}
	
	public static class FileStorer implements ConfigurationStorer
	{
		private final File    file   ;
		private final Charset charset;
		
		FileStorer(
			final File    file   ,
			final Charset charset
		)
		{
			super();
			this.file    = file   ;
			this.charset = charset;
		}
		
		@Override
		public void storeConfiguration(final String configurationData)
		{
			try(OutputStreamWriter writer = new OutputStreamWriter(
				new FileOutputStream(this.file),
				this.charset
			))
			{
				writer.write(configurationData);
				writer.flush();
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
	}
	
}
