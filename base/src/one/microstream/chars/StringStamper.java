package one.microstream.chars;

public interface StringStamper
{
	public String stampString(char[] chars, int offset, int length);



	public final class Default implements StringStamper
	{
		@Override
		public final String stampString(final char[] chars, final int offset, final int length)
		{
			return new String(chars, offset, length);
		}

	}

}
