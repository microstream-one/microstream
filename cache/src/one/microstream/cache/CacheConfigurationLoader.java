
package one.microstream.cache;

import static one.microstream.X.notNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import one.microstream.chars.XChars;
import one.microstream.storage.exceptions.StorageExceptionIo;


@FunctionalInterface
public interface CacheConfigurationLoader
{
	public String loadConfiguration();
	
	public static String loadFromPath(final Path path)
	{
		return loadFromPath(path, Defaults.defaultCharset());
	}
	
	public static String loadFromPath(final Path path, final Charset charset)
	{
		try(InputStream in = Files.newInputStream(path))
		{
			return FromInputStream(in, charset).loadConfiguration();
		}
		catch(final IOException e)
		{
			throw new StorageExceptionIo(e);
		}
	}
	
	public static String loadFromFile(final File file)
	{
		return loadFromFile(file, Defaults.defaultCharset());
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
		return loadFromUrl(url, Defaults.defaultCharset());
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
	
	public static CacheConfigurationLoader FromInputStream(final InputStream inputStream)
	{
		return FromInputStream(inputStream, Defaults.defaultCharset());
	}
	
	public static CacheConfigurationLoader FromInputStream(final InputStream inputStream, final Charset charset)
	{
		return new InputStreamConfigurationLoader(
			notNull(inputStream),
			notNull(charset)
		);
	}
	
	
	public interface Defaults
	{
		public static Charset defaultCharset()
		{
			return StandardCharsets.UTF_8;
		}
	}
	
	
	public static class InputStreamConfigurationLoader implements CacheConfigurationLoader
	{
		private final InputStream inputStream;
		private final Charset     charset;
		
		InputStreamConfigurationLoader(final InputStream inputStream, final Charset charset)
		{
			super();
			
			this.inputStream = inputStream;
			this.charset     = charset;
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
