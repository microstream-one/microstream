
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


public interface ConfigurationParser 
{
	public default Configuration parse(String data)
	{
		return parse(Configuration.Default(),data);
	}
	
	public Configuration parse(Configuration configuration, String data);
	
	
	public static ConfigurationParser Ini()
	{
		return Ini(ConfigurationPropertyParser.New());
	}	
	
	public static ConfigurationParser Ini(ConfigurationPropertyParser propertyParser)
	{
		return new IniConfigurationParser(propertyParser);
	}
	
	public static ConfigurationParser Xml()
	{
		return Xml(ConfigurationPropertyParser.New());
	}	
	
	public static ConfigurationParser Xml(ConfigurationPropertyParser propertyParser)
	{
		return new XmlConfigurationParser(propertyParser);
	}
	
	
	public static class IniConfigurationParser implements ConfigurationParser
	{
		private final ConfigurationPropertyParser propertyParser;

		public IniConfigurationParser(ConfigurationPropertyParser propertyParser) 
		{
			super();
			this.propertyParser = notNull(propertyParser);
		}
		
		@Override
		public Configuration parse(Configuration configuration, String data) 
		{
			if(configuration == null)
			{
				configuration = Configuration.Default();
			}
			
			Properties properties = new Properties();
			try 
			{
				properties.load(new StringReader(data));
			} 
			catch (IOException e) 
			{
				throw new StorageExceptionIo(e);
			}
			removeSections(properties);
			
			for(Object key : properties.keySet()) 
			{
				String name  = (String)key;
				String value = properties.getProperty(name);
				this.propertyParser.parseProperty(name, value, configuration);
			}
			
			return configuration;
		}
		
		/**
		 * Removes ini [section]s from properties, java.util.Properties doesn't parse them properly.
		 */
		protected void removeSections(Properties properties)
		{
			Pattern sectionPattern = Pattern.compile("(?ms)^\\[[^]\\r\\n]+](?:(?!^\\[[^]\\r\\n]+]).)*");
			List<Object> sectionKeys = properties.keySet().stream()
					.filter(key -> sectionPattern.matcher((String)key).matches())
					.collect(Collectors.toList());
			sectionKeys.forEach(properties::remove);
		}
	}
	
	
	public static class XmlConfigurationParser implements ConfigurationParser
	{
		private final ConfigurationPropertyParser propertyParser;

		public XmlConfigurationParser(ConfigurationPropertyParser propertyParser) 
		{
			super();
			this.propertyParser = notNull(propertyParser);
		}
		
		@Override
		public Configuration parse(Configuration configuration, String data) 
		{
			if(configuration == null)
			{
				configuration = Configuration.Default();
			}
			
			try 
			{
				final DocumentBuilder        builder   = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				final Document               document  = builder.parse(new InputSource(new StringReader(data)));
				final Element                documentElement;
				if((documentElement = document.getDocumentElement()) != null)
				{
					NodeList propertyNodes = documentElement.getElementsByTagName("property");
					for(int i = 0, c = propertyNodes.getLength(); i < c; i++)
					{
						Element propertyElement = (Element)propertyNodes.item(i);
						String name             = notEmpty(propertyElement.getAttribute("name"));
						String value            = notEmpty(propertyElement.getAttribute("value"));
						this.propertyParser.parseProperty(name, value, configuration);
					}
				}
			}
			catch (ParserConfigurationException | SAXException e) 
			{
				throw new StorageExceptionInvalidConfiguration(e);
			}
			catch (IOException e) 
			{
				throw new StorageExceptionIo(e);
			}
			
			return configuration;
		}
	}
}
