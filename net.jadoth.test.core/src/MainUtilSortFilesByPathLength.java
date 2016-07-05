import java.io.File;

import net.jadoth.collections.BulkList;
import net.jadoth.util.chars.VarString;


public class MainUtilSortFilesByPathLength
{
	public static void main(final String[] args)
	{
		doIt(new File(""));
	}
	
	
	static void doIt(final File rootDirectory)
	{
		final BulkList<String> collector = BulkList.New(1<<20);
		doIt(rootDirectory, collector);
		collector.sort((s1, s2) -> s2.length() - s1.length());
		
		final VarString vs = VarString.New(collector.intSize() * 200);
		
		for(final String s : collector)
		{
			vs.padLeft(Integer.toString(s.length()), 10, ' ').blank().add(s).lf();
		}
		System.out.println(vs.toString());
	}
	
	static void doIt(final File rootDirectory, final BulkList<String> collector)
	{
		for(final File f : rootDirectory.listFiles())
		{
			collector.add(f.getPath());
			if(f.isDirectory())
			{
				doIt(f, collector);
			}
		}
	}
}
