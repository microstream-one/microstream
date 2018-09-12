import net.jadoth.low.XVM;


public class MainTestPointerRange
{
	public static void main(final String[] args)
	{
		final int pageSize = XVM.pageSize();
		
		
		while(true)
		{
			final long pointer = XVM.allocate(pageSize);
			System.out.println(pointer);
		}
	}
}
