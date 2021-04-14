package one.microstream.storage.restclient.types;

import java.util.function.BiFunction;

import one.microstream.chars.VarString;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XTable;
import one.microstream.storage.restadapter.types.ViewerObjectDescription;

public interface ValueRenderer extends BiFunction<String, ViewerObjectDescription, String>
{
	public static Provider DefaultProvider()
	{
		final ValueRenderer stringLiteralRenderer    = ValueRenderer.StringLiteral   ();
		final ValueRenderer characterLiteralRenderer = ValueRenderer.CharacterLiteral();
		
		final XTable<String, ValueRenderer> valueRenderers = EqHashTable.New();
		valueRenderers.put(String.class.getName()       , stringLiteralRenderer   );
		valueRenderers.put(StringBuffer.class.getName() , stringLiteralRenderer   );
		valueRenderers.put(StringBuilder.class.getName(), stringLiteralRenderer   );
		valueRenderers.put(VarString.class.getName()    , stringLiteralRenderer   );
		valueRenderers.put(char.class.getName()         , characterLiteralRenderer);
		
		return new Provider.Default(
			valueRenderers.immure(), 
			ValueRenderer.Default()
		);
	}
	
	
	public static interface Provider
	{
		public ValueRenderer provideValueRenderer(
			String typeName
		);
		
		
		public static class Default implements Provider
		{
			private final XGettingTable<String, ValueRenderer> valueRenderers;
			private final ValueRenderer                        defaultRenderer;
			
			Default(
				final XGettingTable<String, ValueRenderer> valueRenderers,
				final ValueRenderer defaultRenderer
			)
			{
				super();
			
				this.valueRenderers  = valueRenderers;
				this.defaultRenderer = defaultRenderer;
			}
			
			@Override
			public ValueRenderer provideValueRenderer(
				final String typeName
			)
			{
				final ValueRenderer renderer = this.valueRenderers.get(typeName);
				return renderer != null
					? renderer
					: this.defaultRenderer;
			}
		}
	}
	
	
	public static ValueRenderer Default()
	{
		return (value, reference) -> value;
	}
		
	public static ValueRenderer StringLiteral()
	{
		return (value, reference) -> {
			
			final VarString vs = VarString.New(value.length() + 2)
				.add('"');
			
			for(int i = 0, len = value.length(); i < len; i++)
			{
				final char ch = value.charAt(i);
				
				switch(ch)
				{
					case '\b':
						vs.add("\\b");
					break;
					case '\t':
						vs.add("\\t");
					break;
					case '\n':
						vs.add("\\n");
					break;
					case '\f':
						vs.add("\\f");
					break;
					case '\r':
						vs.add("\\r");
					break;
					case '\"':
						vs.add("\\\"");
					break;
					case '\\':
						vs.add("\\\\");
					break;
					default:
						vs.add(ch);
					break;
				}
			}
			
			return vs.add('"')
				.toString();
		};
	}
	
	public static ValueRenderer CharacterLiteral()
	{
		return (value, reference) -> {

			final VarString vs = VarString.New(4)
				.add('\'');
			
			final char ch = value.charAt(0);
			switch(ch)
			{
				case '\b':
					vs.add("\\b");
				break;
				case '\t':
					vs.add("\\t");
				break;
				case '\n':
					vs.add("\\n");
				break;
				case '\f':
					vs.add("\\f");
				break;
				case '\r':
					vs.add("\\r");
				break;
				case '\'':
					vs.add("\\'");
				break;
				case '\\':
					vs.add("\\\\");
				break;
				default:
					vs.add(ch);
				break;
			}
			
			return vs.add('\'')
				.toString();
		};
	}
	
}
