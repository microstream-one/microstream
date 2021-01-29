package one.microstream.configuration.types;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import one.microstream.exceptions.IORuntimeException;

public interface ConfigurationParserXml extends ConfigurationParser<Element>
{
	public static ConfigurationParserXml New()
	{
		return new ConfigurationParserXml.Default();
	}
	
	
	public static class Default implements ConfigurationParserXml
	{
		Default()
		{
			super();
		}
		
		@Override
		public Element parseConfiguration(
			final String  data
		)
		{
			try
			{
				final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				try
				{
					factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
					factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
				}
				catch(final IllegalArgumentException e)
				{
					/*
					 * swallow
					 * some implementations don't support attributes, e.g. Android
					 */
				}
				return factory.newDocumentBuilder()
					.parse(new InputSource(new StringReader(data)))
					.getDocumentElement()
				;
			}
			catch(ParserConfigurationException | SAXException e)
			{
				throw new RuntimeException(e);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}
		
	}
	
}
