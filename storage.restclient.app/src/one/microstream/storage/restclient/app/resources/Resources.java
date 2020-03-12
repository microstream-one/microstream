package one.microstream.storage.restclient.app.resources;

import com.vaadin.flow.server.StreamResource;

public final class Resources
{
	public static StreamResource streamResource(final String name, final String path)
	{
		return new StreamResource(name, () -> Resources.class.getResourceAsStream(path));
	}
	
	
	private Resources()
	{
		throw new Error();
	}
}
