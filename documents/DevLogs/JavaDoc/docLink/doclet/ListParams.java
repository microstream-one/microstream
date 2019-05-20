package doclink.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.RootDoc;

// source: https://docs.oracle.com/javase/8/docs/jdk/api/javadoc/doclet/index.html
@Deprecated // just for research
public class ListParams extends Doclet
{

	public static boolean start(final RootDoc root)
	{
		final ClassDoc[] classes = root.classes();
		for(int i = 0; i < classes.length; ++i)
		{
			final ClassDoc cd = classes[i];
			printMembers(cd.constructors());
			printMembers(cd.methods());
		}
		
		return true;
	}

	static void printMembers(final ExecutableMemberDoc[] mems)
	{
		for(int i = 0; i < mems.length; ++i)
		{
			final ParamTag[] params = mems[i].paramTags();
//			System.out.println(mems[i].qualifiedName());
			
			for(int j = 0; j < params.length; ++j)
			{
				System.out.println("   " + params[j].parameterName() + " - " + params[j].parameterComment());
			}
		}
	}
	
}