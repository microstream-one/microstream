package one.microstream.storage.types;

public interface StorageClosableFile extends StorageFile
{
	public boolean isOpen();
	
	public boolean close();
	
	
	// (02.12.2019 TM)NOTE: intentionally no single-argument alternative to hint to proper cause handling :).
	public static void close(final StorageClosableFile file, final Throwable cause)
	{
		if(file == null)
		{
			return;
		}
		
		try
		{
			file.close();
		}
		catch(final Throwable t)
		{
			if(cause != null)
			{
				t.addSuppressed(cause);
			}
			throw t;
		}
	}
	
}
