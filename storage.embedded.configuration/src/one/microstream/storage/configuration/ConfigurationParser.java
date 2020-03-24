
package one.microstream.storage.configuration;

import static one.microstream.X.notNull;
import static one.microstream.chars.XChars.notEmpty;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

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
	public default Configuration parse(
		final String data
	)
	{
		return this.parse(Configuration.Default(), data);
	}
	
	
	public Configuration parse(
		Configuration configuration, 
		String data
	);
	
	
	public static ConfigurationParser Ini()
	{
		return Ini(ConfigurationPropertyParser.New());
	}
	
	public static ConfigurationParser Ini(
		final ConfigurationPropertyParser propertyParser
	)
	{
		return new IniConfigurationParser(notNull(propertyParser));
	}
	
	public static ConfigurationParser Xml()
	{
		return Xml(ConfigurationPropertyParser.New());
	}
	
	public static ConfigurationParser Xml(
		final ConfigurationPropertyParser propertyParser
	)
	{
		return new XmlConfigurationParser(notNull(propertyParser));
	}
	
	
	public static class IniConfigurationParser implements ConfigurationParser
	{
		private final ConfigurationPropertyParser propertyParser;
		
		IniConfigurationParser(
			final ConfigurationPropertyParser propertyParser
		)
		{
			super();
			this.propertyParser = propertyParser;
		}
		
		@Override
		public Configuration parse(
			final Configuration configuration,
			final String data
		)
		{
			final Map<String, String> properties = new HashMap<>();
			
			nextLine:
			for(String line : data.split("\\r?\\n"))
			{
				line = line.trim();
				if(line.isEmpty())
				{
					continue nextLine;
				}
				
				switch(line.charAt(0))
				{
					case '#': // comment
					case ';': // comment
					case '[': // section
						continue nextLine;
				}
				
				final int separatorIndex = line.indexOf('=');
				if(separatorIndex == -1)
				{
					continue nextLine; // no key=value pair, ignore
				}
				
				final String name  = line.substring(0, separatorIndex).trim();
				final String value = line.substring(separatorIndex + 1).trim();
				properties.put(name, value);
			}
			
			this.propertyParser.parseProperties(properties, configuration);
			
			return configuration;
		}
		
	}
	
	public static class XmlConfigurationParser implements ConfigurationParser
	{
		private final ConfigurationPropertyParser propertyParser;
		
		XmlConfigurationParser(
			final ConfigurationPropertyParser propertyParser
		)
		{
			super();
			this.propertyParser = propertyParser;
		}
		
		@Override
		public Configuration parse(
			final Configuration configuration, 
			final String data
		)
		{
			try
			{
				final Map<String, String> properties = new HashMap<>();
				
				final DocumentBuilder builder  = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				final Document        document = builder.parse(new InputSource(new StringReader(data)));
				final Element         documentElement;
				if((documentElement = document.getDocumentElement()) != null)
				{
					final NodeList propertyNodes = documentElement.getElementsByTagName("property");
					for(int i = 0, c = propertyNodes.getLength(); i < c; i++)
					{
						final Element propertyElement = (Element)propertyNodes.item(i);
						final String  name            = notEmpty(propertyElement.getAttribute("name").trim());
						final String  value           = notEmpty(propertyElement.getAttribute("value").trim());
						properties.put(name, value);
					}
				}
				
				this.propertyParser.parseProperties(properties, configuration);
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
