package one.microstream.configuration.types;

import java.io.IOException;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import one.microstream.chars.VarString;
import one.microstream.configuration.exceptions.ConfigurationException;

/**
 * Assembler for configurations to export to XML format.
 * 
 * @see Configuration#store(ConfigurationStorer, ConfigurationAssembler)
 */
public interface ConfigurationAssemblerXml extends ConfigurationAssembler
{
	/**
	 * Pseudo-constructor to create a new XML assembler.
	 * 
	 * @return a new XML assembler
	 */
	public static ConfigurationAssemblerXml New()
	{
		return new ConfigurationAssemblerXml.Default();
	}
	
	
	public static class Default implements ConfigurationAssemblerXml
	{
		Default()
		{
			super();
		}
		
		@Override
		public VarString assemble(
			final VarString     vs           ,
			final Configuration configuration
		)
		{
			try
			{
				final Document document    = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				final Element  rootElement = document.createElement("configuration");
				document.appendChild(rootElement);
				
				this.assemble(document, rootElement, configuration);
				
				final Transformer  transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
				transformer.transform(
					new DOMSource(document),
					new StreamResult(new VarStringWriter(vs))
				);
			}
			catch(TransformerException | TransformerFactoryConfigurationError | ParserConfigurationException e)
			{
				throw new ConfigurationException(configuration, e);
			}
			
			
			return vs;
		}
		
		private void assemble(
			final Document      document     ,
			final Element       parent       ,
			final Configuration configuration
		)
		{
			configuration.keys().forEach(key ->
			{
				final Element element = document.createElement(key);
				element.appendChild(document.createTextNode(configuration.get(key)));
				parent.appendChild(element);
			});
			
			configuration.children().forEach(child ->
			{
				final Element element = document.createElement(child.key());
				parent.appendChild(element);
				this.assemble(document, element, child);
			});
		}


		static class VarStringWriter extends Writer
		{
			final VarString vs;

			VarStringWriter(final VarString vs)
			{
				super();
				this.vs = vs;
			}

			@Override
			public void write(final char[] cbuf, final int off, final int len) throws IOException
			{
				this.vs.add(cbuf, off, len);
			}

			@Override
			public void flush() throws IOException
			{
				// no-op
			}

			@Override
			public void close() throws IOException
			{
				// no-op
			}

		}
		
	}
	
}
