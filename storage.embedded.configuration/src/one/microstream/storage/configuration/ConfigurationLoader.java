
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


public interface ConfigurationLoader
{
	public String loadConfiguration();
	
	
	public static String loadFromFile(File file)
	{
		return loadFromFile(file, null);
	}
	
	
	public static String loadFromFile(File file, Charset charset)
	{
		try(InputStream in = new FileInputStream(file))
		{
			return FromInputStream(in, charset).loadConfiguration();
		}
		catch(IOException e)
		{
			throw new StorageExceptionIo(e);
		}		
	}
	
	
	public static String loadFromUrl(URL url)
	{
		return loadFromUrl(url, null);
	}
	
	
	public static String loadFromUrl(URL url, Charset charset)
	{
		try(InputStream in = url.openStream())
		{
			return FromInputStream(in, charset).loadConfiguration();
		}
		catch(IOException e)
		{
			throw new StorageExceptionIo(e);
		}		
	}
	
	
	public static ConfigurationLoader FromInputStream(final InputStream inputStream)
	{
		return FromInputStream(inputStream, null);
	}
	
	
	public static ConfigurationLoader FromInputStream(final InputStream inputStream, Charset charset)
	{
		return new InputStreamConfigurationLoader(inputStream, charset);
	}
	
	
	
	public static class InputStreamConfigurationLoader implements ConfigurationLoader
	{
		private final InputStream inputStream;
		private final Charset     charset;
		
		public InputStreamConfigurationLoader(final InputStream inputStream, Charset charset)
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
				return XChars.readStringFromInputStream(this.inputStream,this.charset);
			}
			catch(final IOException e)
			{
				throw new StorageExceptionIo(e);
			}
		}
	}
}
