package one.microstream.configuration.types;

import static one.microstream.chars.XChars.notEmpty;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import one.microstream.chars.XChars;
import one.microstream.configuration.types.Configuration.Builder;

/**
 * Mapper which maps entries from a XML {@link Element} to a {@link Configuration#Builder()}.
 *
 */
public interface ConfigurationMapperXml extends ConfigurationMapper<Element>
{
	/**
	 * Pseudo-constructor to create a new mapper.
	 * 
	 * @return a new mapper
	 */
	public static ConfigurationMapperXml New()
	{
		return new ConfigurationMapperXml.Default();
	}
	
	
	public static class Default implements ConfigurationMapperXml
	{
		Default()
		{
			super();
		}
		
		@Override
		public Builder mapConfiguration(
			final Builder builder,
			final Element source
		)
		{
			if(this.isFlatLayout(source))
			{
				this.mapFlat(builder, source);
			}
			else
			{
				this.mapDeep(builder, source, "");
			}
			
			return builder;
		}
		
		private boolean isFlatLayout(
			final Element source
		)
		{
			if(!source.getTagName().equalsIgnoreCase("properties"))
			{
				return false;
			}
			
			final NodeList childNodes = source.getChildNodes();
			for(int i = 0, c = childNodes.getLength(); i < c; i++)
			{
				final Node node = childNodes.item(i);
				if(node instanceof Element)
				{
					final Element element = (Element)node;
					if(!element.getTagName().equalsIgnoreCase("property"))
					{
						return false;
					}
				}
			}
			
			return true;
		}
		
		private void mapFlat(
			final Builder builder,
			final Element source
		)
		{
			final NodeList propertyNodes = source.getElementsByTagName("property");
			for(int i = 0, c = propertyNodes.getLength(); i < c; i++)
			{
				final Element propertyElement = (Element)propertyNodes.item(i);
				final String  name            = notEmpty(propertyElement.getAttribute("name").trim());
				final String  value           = notEmpty(propertyElement.getAttribute("value").trim());
				builder.set(name, value);
			}
		}
				
		private void mapDeep(
			final Configuration.Builder builder        ,
			final Element               documentElement,
			final String                prefix
		)
		{
			final NodeList childNodes = documentElement.getChildNodes();
			for(int i = 0, c = childNodes.getLength(); i < c; i++)
			{
				final Node node = childNodes.item(i);
				if(node instanceof Element)
				{
					final Element element = (Element)node;
					final String  tagName = element.getTagName();
					final String  key     = prefix.concat(tagName);
					final String  value   = this.getTextValue(element);
					if(value != null)
					{
						builder.set(key, value);
					}
					this.mapDeep(builder, element, key + Configuration.KEY_SEPARATOR);
				}
			}
		}
		
		private String getTextValue(final Element element)
		{
			final NodeList childNodes = element.getChildNodes();
			for(int i = 0, c = childNodes.getLength(); i < c; i++)
			{
				final Node node = childNodes.item(i);
				if(node instanceof Text)
				{
					final String text = node.getTextContent().trim();
					if(!XChars.isEmpty(text))
					{
						return text;
					}
				}
			}
			
			return null;
		}
		
	}
	
}
