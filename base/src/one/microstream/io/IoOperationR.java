package one.microstream.io;

import java.io.IOException;


@FunctionalInterface
public interface IoOperationR<R>
{
	public R executeR() throws IOException;
}
