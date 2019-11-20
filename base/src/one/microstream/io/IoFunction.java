package one.microstream.io;

import java.io.IOException;

public interface IoFunction<S, T>
{
	public T performOperation(S subject) throws IOException;
}
