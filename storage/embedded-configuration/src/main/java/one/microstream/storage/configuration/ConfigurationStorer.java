package one.microstream.storage.configuration;

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

/**
 *
 * @deprecated will be removed in a future release
 * @see one.microstream.storage.configuration
 */
@Deprecated
public interface ConfigurationStorer
{
	public void storeConfiguration(String configurationData);


	public static void storeToPath(
		final Path    path             ,
		final String  configurationData
	)
	{
		storeToPath(path, Defaults.defaultCharset(), configurationData);
	}

	public static void storeToPath(
		final Path    path             ,
		final Charset charset          ,
		final String  configurationData
	)
	{
		try(OutputStream out = Files.newOutputStream(path))
		{
			ToOutputStream(out, charset).storeConfiguration(configurationData);
		}
		catch(final IOException e)
		{
			throw new StorageConfigurationIoException(e);
		}
	}

	public static void storeToFile(
		final File    file             ,
		final String  configurationData
	)
	{
		storeToFile(file, Defaults.defaultCharset(), configurationData);
	}

	public static void storeToFile(
		final File    file             ,
		final Charset charset          ,
		final String  configurationData
	)
	{
		try(FileOutputStream out = new FileOutputStream(file))
		{
			ToOutputStream(out, charset).storeConfiguration(configurationData);
		}
		catch(final IOException e)
		{
			throw new StorageConfigurationIoException(e);
		}
	}

	public static void storeToUrl(
		final URL     url              ,
		final String  configurationData
	)
	{
		storeToUrl(url, Defaults.defaultCharset(), configurationData);
	}

	public static void storeToUrl(
		final URL     url              ,
		final Charset charset          ,
		final String  configurationData
	)
	{
		try
		{
			final URLConnection urlConnection = url.openConnection();
			urlConnection.setDoOutput(true);
			try(OutputStream out = urlConnection.getOutputStream())
			{
				ToOutputStream(out, charset).storeConfiguration(configurationData);
			}
		}
		catch(final IOException e)
		{
			throw new StorageConfigurationIoException(e);
		}
	}


	public static ConfigurationStorer ToOutputStream(
		final OutputStream outputStream
	)
	{
		return new ConfigurationStorerOutputStream(
			outputStream,
			Defaults.defaultCharset()
		);
	}

	public static ConfigurationStorer ToOutputStream(
		final OutputStream outputStream,
		final Charset      charset
	)
	{
		return new ConfigurationStorerOutputStream(
			notNull(outputStream)   ,
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


	public static class ConfigurationStorerOutputStream implements ConfigurationStorer
	{
		private final OutputStream outputStream;
		private final Charset      charset     ;

		ConfigurationStorerOutputStream(
			final OutputStream    outputStream,
			final Charset         charset
		)
		{
			super();
			this.outputStream = outputStream;
			this.charset      = charset     ;
		}

		@Override
		public void storeConfiguration(
			final String configurationData
		)
		{
			try(OutputStreamWriter writer = new OutputStreamWriter(this.outputStream, this.charset))
			{
				writer.write(configurationData);
				writer.flush();
			}
			catch(final IOException e)
			{
				throw new StorageConfigurationIoException(e);
			}
		}

	}

}
