package one.microstream.io;

import java.io.IOException;


@FunctionalInterface
public interface IoOperation
{
	public void execute() throws IOException;
}
