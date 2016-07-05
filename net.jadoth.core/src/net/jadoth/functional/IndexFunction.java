package net.jadoth.functional;


public interface IndexFunction<I, O>
{
	public O apply(I input, int index);

}
