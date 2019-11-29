package one.microstream.io;

import java.io.IOException;


@FunctionalInterface
public interface IoOperationS<S>
{
	public void executeS(S subject) throws IOException;
}
