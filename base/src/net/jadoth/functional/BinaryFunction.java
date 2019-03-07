package net.jadoth.functional;

/**
 *
 * @author Thomas Muenz
 *
 * @param <E>
 */
public interface BinaryFunction<I1, I2, O>
{
	public O apply(I1 i1, I2 i2);
}
