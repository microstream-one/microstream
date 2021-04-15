
package one.microstream.examples.blobs;

import java.util.UUID;

public class FileAsset
{
	private final String path;
	private final String name;
	private final String uuid;
	
	public FileAsset(final String path, final String name)
	{
		super();
		
		this.path = path;
		this.name = name;
		this.uuid = UUID.randomUUID().toString();
	}
	
	public String getPath()
	{
		return this.path;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getUUID()
	{
		return this.uuid;
	}
}
