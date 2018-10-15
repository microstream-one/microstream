package net.jadoth.storage.io;

public interface ProtageChannelDataFile extends ProtageChannelFile
{
	public long number();
	
	@Override
	public long length();
	
	public long dataLength();

	public default double dataFillRatio()
	{
		synchronized(this)
		{
			return (double)this.dataLength() / this.length();
		}
	}
	
	public boolean isHeadFile();
	
}
