
package one.microstream.examples.blobs;

import java.io.File;

public class MyRoot
{
	private final FileAssets fileAssets = new FileAssets(new File("assets"));
	
	public MyRoot()
	{
		super();
	}
	
	public FileAssets getFileAssets()
	{
		return this.fileAssets;
	}
}
