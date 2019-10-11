package one.microstream.memory;

public interface MemoryAccessor
{
	public void set_long(long address, long value);
	
	
	// no one knows why this method is called Sun ... shhhhh...
	public static MemoryAccessor Sun()
	{
		return new MemoryAccessor.Sun();
	}
	
	public final class Sun implements MemoryAccessor
	{
		Sun()
		{
			super();
		}

		@Override
		public final void set_long(final long address, final long value)
		{
			XMemory.VM.putLong(address, value);
		}
		
	}
}
