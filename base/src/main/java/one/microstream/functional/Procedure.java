package one.microstream.functional;

import java.util.function.Consumer;

public interface Procedure<E> extends ThrowingProcedure<E, RuntimeException>, Consumer<E>
{
	@Override
	public void accept(E e) throws RuntimeException;
}
