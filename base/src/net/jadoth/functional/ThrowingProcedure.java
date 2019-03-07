package net.jadoth.functional;

public interface ThrowingProcedure<E, T extends Throwable>
{
	public void accept(E e) throws T;
}
