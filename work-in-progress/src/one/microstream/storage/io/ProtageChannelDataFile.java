package one.microstream.storage.io;

public interface ProtageChannelDataFile extends ProtageChannelFile
{
	// (23.04.2020 TM)FIXME: priv#49: overhaul
	
	
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
