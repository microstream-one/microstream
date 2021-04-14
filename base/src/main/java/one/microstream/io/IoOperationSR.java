package one.microstream.io;

import java.io.IOException;


@FunctionalInterface
public interface IoOperationSR<S, R>
{
	public R executeSR(S subject) throws IOException;
}
