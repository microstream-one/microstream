package net.jadoth.functional;


public interface IndexedFunction<I, O>
{
	public O apply(I input, int index);

}
