package one.microstream.storage.restclient.types;

import java.util.function.Function;

import one.microstream.chars.VarString;

public interface ValueRenderer extends Function<String, String>
{
	public static ValueRenderer StringLiteral()
	{
		return value -> {
			
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
		return value -> {

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
