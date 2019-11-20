package one.microstream.io;

import java.io.IOException;

public interface IoOperation<T>
{
	public T performOperation() throws IOException;
}
