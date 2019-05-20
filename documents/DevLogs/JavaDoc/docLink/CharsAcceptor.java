package doclink;

public interface CharsAcceptor
{
	public void acceptChars(String input);

	public void acceptChar(char c);

	// default is optimized for expecting String instances, but can be implemented the other way around just as well.
	
	public default void acceptChars(final CharSequence input, final int offset, final int length)
	{
		this.acceptChars(input.subSequence(offset, offset + length).toString());
	}
	
	public default void acceptChars(final char[] input, final int offset, final int length)
	{
		this.acceptChars(String.copyValueOf(input, offset, length));
	}
	
}
