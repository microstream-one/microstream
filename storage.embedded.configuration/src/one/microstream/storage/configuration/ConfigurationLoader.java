
package one.microstream.storage.configuration;

import static one.microstream.X.notNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import one.microstream.chars.XChars;
import one.microstream.storage.exceptions.StorageExceptionIo;


@FunctionalInterface
public interface ConfigurationLoader
{
	public String loadConfiguration();
	
	public static String loadFromFile(final File file)
	{
		return loadFromFile(file, null);
	}
	
	public static String loadFromFile(final File file, final Charset charset)
	{
		try(InputStream in = new FileInputStream(file))
		{
			return FromInputStream(in, charset).loadConfiguration();
		}
		catch(final IOException e)
		{
			throw new StorageExceptionIo(e);
		}
	}
	
	public static String loadFromUrl(final URL url)
	{
		return loadFromUrl(url, null);
	}
	
	public static String loadFromUrl(final URL url, final Charset charset)
	{
		try(InputStream in = url.openStream())
		{
			return FromInputStream(in, charset).loadConfiguration();
		}
		catch(final IOException e)
		{
			throw new StorageExceptionIo(e);
		}
	}
	
	public static ConfigurationLoader FromInputStream(final InputStream inputStream)
	{
		return FromInputStream(inputStream, null);
	}
	
	public static ConfigurationLoader FromInputStream(final InputStream inputStream, final Charset charset)
	{
		return new InputStreamConfigurationLoader(inputStream, charset);
	}
	
	public static class InputStreamConfigurationLoader implements ConfigurationLoader
	{
		private final InputStream inputStream;
		private final Charset     charset;
		
		protected InputStreamConfigurationLoader(final InputStream inputStream, final Charset charset)
		{
			super();
			
			this.inputStream = notNull(inputStream);
			this.charset     = charset != null ? charset : StandardCharsets.UTF_8;
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
				throw new StorageExceptionIo(e);
			}
		}
	}
}
