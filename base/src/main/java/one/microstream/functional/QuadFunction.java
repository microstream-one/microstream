package one.microstream.functional;

public interface QuadFunction<I1, I2, I3, I4, O>
{
	public O apply(I1 t1, I2 t2, I3 t3, I4 t4);
}
