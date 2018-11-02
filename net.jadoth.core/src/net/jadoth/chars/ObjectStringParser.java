package net.jadoth.chars;


public interface ObjectStringParser<T>
{
	public T parse(_charArrayRange input);

	public default T parse(final String input)
	{
		return this.parse(_charArrayRange.New(input));
	}

}
