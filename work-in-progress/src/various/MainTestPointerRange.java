package various;
import one.microstream.memory.XMemory;


public class MainTestPointerRange
{
	public static void main(final String[] args)
	{
		final int pageSize = XMemory.pageSize();
		
		
		while(true)
		{
			final long pointer = XMemory.allocate(pageSize);
			System.out.println(pointer);
		}
	}
}
