package one.microstream.storage.restservice;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class MyImpl implements MyInterface
{
	private final ArrayList<String> data = new ArrayList<>();

	public MyImpl(final String ...strings)
	{
		for (final String string : strings)
		{
			this.data.add(string.toUpperCase());
		}
	}

	@Override
	public String getValue()
	{
		return this.data.stream().collect(Collectors.joining(";"));
	}

}
