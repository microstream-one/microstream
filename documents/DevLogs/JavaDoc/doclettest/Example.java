package doclettest;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTrees;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;


// source: https://docs.oracle.com/javase/9/docs/api/jdk/javadoc/doclet/package-summary.html
public class Example implements Doclet
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	Reporter reporter    ;
	String   overviewfile;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public String getName()
	{
		return "Example";
	}
	
	@Override
	public SourceVersion getSupportedSourceVersion()
	{
		// support the latest release
		return SourceVersion.latest();
	}
	
	@Override
	public void init(final Locale locale, final Reporter reporter)
	{
		reporter.print(Kind.NOTE, "Doclet using locale: " + locale);
		this.reporter = reporter;
	}
	
	@Override
	public boolean run(final DocletEnvironment docEnv)
	{
		this.reporter.print(Kind.NOTE, "overviewfile: " + this.overviewfile);
		// get the DocTrees utility class to access document comments
		final DocTrees docTrees = docEnv.getDocTrees();
		
		// location of an element in the same directory as overview.html
		try
		{
			// (13.05.2019 TM)NOTE: the auther clearly doesn't know how to use an Iterator. had to fix it.
			final Iterator<TypeElement> typeElementIterator = ElementFilter.typesIn(docEnv.getSpecifiedElements()).iterator();
			if(typeElementIterator.hasNext())
			{
				final Element e = typeElementIterator.next();
				final DocCommentTree docCommentTree = docTrees.getDocCommentTree(e, this.overviewfile);
				if(docCommentTree != null)
				{
					System.out.println("Overview html: " + docCommentTree.getFullBody());
				}
			}
			else
			{
				// (13.05.2019 TM)NOTE: No idea if that's print-worthy , but here it is.
				System.out.println("[No docEnv specific elements]");
			}
			
		}
		catch(final IOException missing)
		{
			this.reporter.print(Kind.ERROR, "No overview.html found.");
		}
		
		for (final TypeElement t : ElementFilter.typesIn(docEnv.getIncludedElements()))
		{
			System.out.println(t.getKind() + ":" + t);
			for (final Element e : t.getEnclosedElements())
			{
				printElement(docTrees, e);
			}
		}
		return true;
	}
	
	static void printElement(final DocTrees trees, final Element e)
	{
		final DocCommentTree docCommentTree = trees.getDocCommentTree(e);
		if(docCommentTree != null)
		{
			System.out.println("Element (" + e.getKind() + ": "+ e + ") has the following comments:");
			System.out.println("Entire body: " + docCommentTree.getFullBody());
			System.out.println("Block tags: " + docCommentTree.getBlockTags());
		}
	}
	
	@Override
	public Set<? extends Option> getSupportedOptions()
	{
		final Option[] options =
		{
			new Option()
			{
				private final List<String> someOption = Arrays.asList(
					"-overviewfile",
					"--overview-file",
					"-o"
				);
				
				@Override
				public int getArgumentCount()
				{
					return 1;
				}
				
				@Override
				public String getDescription()
				{
					return "an option with aliases";
				}
				
				@Override
				public Option.Kind getKind()
				{
					return Option.Kind.STANDARD;
				}
				
				@Override
				public List<String> getNames()
				{
					return this.someOption;
				}
				
				@Override
				public String getParameters()
				{
					return "file";
				}
				
				@Override
				public boolean process(final String opt, final List<String> arguments)
				{
					// (13.05.2019 TM)NOTE: to bring some light in the weirdness of this method
					System.out.println("Doclet processing arguments: \"opt\" = >" + opt + "<, arguments:");
					arguments.forEach(System.out::println);
					
					/* (13.05.2019 TM)NOTE:
					 * No idea who was smoking what when writing this error-ridden class, but the
					 * moronically-named "arguments" List seems to be a list of (maybe top-level) packages,
					 * and not something where the first argument is a suitable value for something named
					 * "overviewfile".
					 * Just ignore it.
					 */
					Example.this.overviewfile = arguments.get(0);
					return true;
				}
			}
		};
		
		return new HashSet<>(Arrays.asList(options));
	}
	
}
