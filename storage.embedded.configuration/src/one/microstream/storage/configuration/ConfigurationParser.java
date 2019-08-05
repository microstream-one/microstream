
package one.microstream.storage.configuration;

import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import one.microstream.storage.exceptions.StorageExceptionInvalidConfiguration;
import one.microstream.storage.exceptions.StorageExceptionIo;


@FunctionalInterface
public interface ConfigurationParser
{
	public default Configuration parse(final String data)
	{
		return this.parse(Configuration.Default(), data);
	}
	
	public Configuration parse(Configuration configuration, String data);
	
	public static ConfigurationParser Ini()
	{
		return Ini(ConfigurationPropertyParser.New());
	}
	
	public static ConfigurationParser Ini(final ConfigurationPropertyParser propertyParser)
	{
		return new IniConfigurationParser(propertyParser);
	}
	
	public static ConfigurationParser Xml()
	{
		return Xml(ConfigurationPropertyParser.New());
	}
	
	public static ConfigurationParser Xml(final ConfigurationPropertyParser propertyParser)
	{
		return new XmlConfigurationParser(propertyParser);
	}
	
	public static class IniConfigurationParser implements ConfigurationParser
	{
		private final ConfigurationPropertyParser propertyParser;
		
		protected IniConfigurationParser(final ConfigurationPropertyParser propertyParser)
		{
			super();
			this.propertyParser = notNull(propertyParser);
		}
		
		@Override
		public Configuration parse(Configuration configuration, final String data)
		{
			if(configuration == null)
			{
				configuration = Configuration.Default();
			}
			
			final Properties properties = new Properties();
			try
			{
				properties.load(new StringReader(data));
			}
			catch(final IOException e)
			{
				throw new StorageExceptionIo(e);
			}
			this.removeSections(properties);
			
			for(final Object key : properties.keySet())
			{
				final String name  = (String)key;
				final String value = properties.getProperty(name);
				this.propertyParser.parseProperty(name, value, configuration);
			}
			
			return configuration;
		}
		
		/**
		 * Removes ini [section]s from properties, java.util.Properties doesn't parse them properly.
		 */
		protected void removeSections(final Properties properties)
		{
			final Pattern      sectionPattern = Pattern.compile("(?ms)^\\[[^]\\r\\n]+](?:(?!^\\[[^]\\r\\n]+]).)*");
			final List<Object> sectionKeys    = properties.keySet().stream()
				.filter(key -> sectionPattern.matcher((String)key).matches())
				.collect(Collectors.toList());
			sectionKeys.forEach(properties::remove);
		}
	}
	
	public static class XmlConfigurationParser implements ConfigurationParser
	{
		private final ConfigurationPropertyParser propertyParser;
		
		protected XmlConfigurationParser(final ConfigurationPropertyParser propertyParser)
		{
			super();
			this.propertyParser = notNull(propertyParser);
		}
		
		@Override
		public Configuration parse(Configuration configuration, final String data)
		{
			if(configuration == null)
			{
				configuration = Configuration.Default();
			}
			
			try
			{
				final DocumentBuilder builder  = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				final Document        document = builder.parse(new InputSource(new StringReader(data)));
				final Element         documentElement;
				if((documentElement = document.getDocumentElement()) != null)
				{
					final NodeList propertyNodes = documentElement.getElementsByTagName("property");
					for(int i = 0, c = propertyNodes.getLength(); i < c; i++)
					{
						final Element propertyElement = (Element)propertyNodes.item(i);
						final String  name            = notEmpty(propertyElement.getAttribute("name"));
						final String  value           = notEmpty(propertyElement.getAttribute("value"));
						this.propertyParser.parseProperty(name, value, configuration);
					}
				}
			}
			catch(ParserConfigurationException | SAXException e)
			{
				throw new StorageExceptionInvalidConfiguration(e);
			}
			catch(final IOException e)
			{
				throw new StorageExceptionIo(e);
			}
			
			return configuration;
		}
	}
}
