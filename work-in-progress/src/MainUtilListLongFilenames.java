import java.io.File;


public class MainUtilListLongFilenames
{
	public static void main(final String[] args)
	{
		doIt(150, new File("D:\\Files"));
	}
	
	
	static void doIt(final int maximumFileLength, final File directory)
	{
		for(final File f : directory.listFiles())
		{
			if(f.isDirectory())
			{
				doIt(maximumFileLength, f);
			}
			else if(f.getName().length() > maximumFileLength)
			{
				System.out.println(f.toString());
			}
		}
	}
}
