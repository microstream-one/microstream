package one.microstream.configuration.types;

import static one.microstream.X.notNull;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import one.microstream.configuration.types.Configuration.Builder;
import one.microstream.exceptions.IORuntimeException;

/**
 * XML format parser for configurations.
 * 
 */
public interface ConfigurationParserXml extends ConfigurationParser
{
	/**
	 * Pseudo-constructor to create a new XML parser.
	 * 
	 * @return a new XML parser
	 */
	public static ConfigurationParserXml New()
	{
		return new ConfigurationParserXml.Default(
			ConfigurationMapperXml.New()
		);
	}
	
	/**
	 * Pseudo-constructor to create a new XML parser.
	 * 
	 * @return a new XML parser
	 */
	public static ConfigurationParserXml New(
		final ConfigurationMapperXml mapper
	)
	{
		return new ConfigurationParserXml.Default(
			notNull(mapper)
		);
	}
	
	
	public static class Default implements ConfigurationParserXml
	{
		private final ConfigurationMapperXml mapper;
		
		Default(
			final ConfigurationMapperXml mapper
		)
		{
			super();
			this.mapper = mapper;
		}
		
		@Override
		public Builder parseConfiguration(
			final Builder builder,
			final String  input
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
				final Element element = factory.newDocumentBuilder()
					.parse(new InputSource(new StringReader(input)))
					.getDocumentElement()
				;
				
				return this.mapper.mapConfiguration(builder, element);
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
