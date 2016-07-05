import net.jadoth.memory.Memory;


public class MainTestPointerRange
{
	public static void main(final String[] args)
	{
		final int pageSize = Memory.pageSize();
		
		
		while(true)
		{
			final long pointer = Memory.allocate(pageSize);
			System.out.println(pointer);
		}
	}
}
