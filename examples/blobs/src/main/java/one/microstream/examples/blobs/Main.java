
package one.microstream.examples.blobs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import one.microstream.exceptions.IORuntimeException;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;


/**
 * 
 * A simple example how to handle blobs (or large binary data).
 * Best practice is to just have a metadata object ({@link FileAsset}) in the persistent object graph,
 * and the actual file data outside in a dedicated directory.
 *
 */
public class Main
{
	public static void main(final String[] args)
	{
		// Init storage manager
		final EmbeddedStorageManager storage = EmbeddedStorage.start();
		
		// if storage.root() returns null no data have been loaded
		// since there is no existing database, let's create a new one.
		if(storage.root() == null)
		{
			System.out.println("No existing Database found, creating a new one:");
			
			final MyRoot root = new MyRoot();
			
			createSampleAsset(root, "testfiles/sample.xml", "sample.xml",
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<samplefile>\n</samplefile>");
			createSampleAsset(root, "testfiles/sample.json", "sample.json", "{\n\t\"number\":1,\n\t\"text\":\"string\"\n}");
			
			storage.setRoot(root);
			storage.storeRoot();
		}
		// storage.root() is not null so we have loaded data
		else
		{
			System.out.println("Existing Database found:");
		}
		
		final MyRoot root = (MyRoot)storage.root();
		for(final FileAsset fileAsset : root.getFileAssets())
		{
			System.out.println("File asset '" + fileAsset.getName() + "' file = "
				+ root.getFileAssets().getAssetFile(fileAsset).getAbsolutePath());
		}
		
		storage.shutdown();
	}
	
	private static void createSampleAsset(final MyRoot root, final String path, final String name, final String contents)
	{
		final FileAsset  asset      = new FileAsset(path, name);
		final FileAssets fileAssets = root.getFileAssets();
		final File       file       = fileAssets.registerFileAsset(asset).getAssetFile(asset);
		try(FileWriter writer = new FileWriter(file))
		{
			writer.write(contents);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}
}
