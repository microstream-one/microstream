package various;
import java.io.File;



public class CheckPics
{
	public static void main(String[] args)
	{
		final File dir = new File("c:/Pics");
		final File[] files = dir.listFiles();
		File curr = null, last = files[0];
		for(int i = 1; i < files.length; i++)
		{
			if((curr = files[i]).length() == last.length())
			{
				System.err.println(curr.getName());
				curr.delete();
				continue;
			}
			last = curr;
		}
	}
}
