package doclink;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;

/*
 * See
 * https://docs.oracle.com/javase/8/docs/jdk/api/javadoc/doclet/com/sun/javadoc/RootDoc.html
 * 
 */
public final class DocLinkTagResolver extends DocLinkTagProcessor.AbstractParserBuffering
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final DocLinkTagResolver New(final StringBuilder sb, final RootDoc docRoot)
	{
		return new DocLinkTagResolver(
			UtilsDocLink.notNull(sb),
			UtilsDocLink.notNull(docRoot)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final RootDoc docRoot;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	DocLinkTagResolver(final StringBuilder sb, final RootDoc docRoot)
	{
		super(sb);
		this.docRoot = docRoot;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	protected void handleParsedContent(final DocLinkTagParts parts)
	{
		final ClassDoc cd = UtilsDocLink.resolveClass(parts.typeName(), this.docRoot);
		
		if(parts.isMember())
		{
			if(parts.isMethod())
			{
				final MethodDoc md = UtilsDocLink.resolveMethod(parts.memberName(), cd, parts.parameterList());
				this.handleMethodDoc(md, parts);
			}
			else
			{
				final FieldDoc fd = UtilsDocLink.resolveField(parts.memberName(), cd);
				this.handleFieldDoc(fd, parts);
			}
		}
		else
		{
			this.handleClassDoc(cd, parts);
		}
	}
		
	private void handleClassDoc(final ClassDoc cd, final DocLinkTagParts parts)
	{
		
	}
	
	private void handleFieldDoc(final FieldDoc fd, final DocLinkTagParts parts)
	{
		
	}
	
	private void handleMethodDoc(final MethodDoc md, final DocLinkTagParts parts)
	{
		
	}
	
}
