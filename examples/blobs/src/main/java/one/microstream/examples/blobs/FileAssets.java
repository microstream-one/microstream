
package one.microstream.examples.blobs;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import one.microstream.persistence.types.Storer;


public class FileAssets implements Iterable<FileAsset>
{
	private final File                   root;
	private final Map<String, FileAsset> registry;
	
	public FileAssets(final File root)
	{
		super();
		
		this.root     = root;
		this.registry = new HashMap<>();
	}
	
	public FileAssets registerFileAsset(final FileAsset fileAsset)
	{
		this.registry.put(fileAsset.getUUID(), fileAsset);
		return this;
	}
	
	public FileAsset getFileAssetById(final String uuid)
	{
		return this.registry.get(uuid);
	}
	
	public FileAsset getFileAssetByName(final String name)
	{
		return this.registry.values().stream()
			.filter(fileAsset -> fileAsset.getName().equals(name))
			.findFirst()
			.orElse(null);
	}
	
	public File getAssetFile(final FileAsset asset)
	{
		final File file = new File(this.root, asset.getPath());
		file.getParentFile().mkdirs();
		return file;
	}
	
	@Override
	public Iterator<FileAsset> iterator()
	{
		return this.registry.values().iterator();
	}
	
	public <S extends Storer> S store(final S storer)
	{
		storer.store(this.registry);
		storer.store(this);
		return storer;
	}
}
