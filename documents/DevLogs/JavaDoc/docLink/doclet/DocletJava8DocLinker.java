package doclink.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

import doclink.CharsAcceptor;
import doclink.DocLinkTagParts;
import doclink.DocLinker;
import doclink.UtilsDocLink;

/*
 * See
 * https://docs.oracle.com/javase/8/docs/jdk/api/javadoc/doclet/com/sun/javadoc/RootDoc.html
 * 
 */
public final class DocletJava8DocLinker extends DocLinker.Abstract
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final DocletJava8DocLinker New(final RootDoc docRoot)
	{
		return new DocletJava8DocLinker(
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
	
	DocletJava8DocLinker(final RootDoc docRoot)
	{
		super();
		this.docRoot = docRoot;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	protected void handleParsedContent(
		final DocLinkTagParts parts        ,
		final String          parameterName,
		final CharsAcceptor   charsAcceptor
	)
	{
		final ClassDoc cd = DocletJava8DocLink.resolveClass(parts.typeName(), this.docRoot);
		
		if(parts.isMember())
		{
			if(parts.isMethod())
			{
				final MethodDoc md = DocletJava8DocLink.resolveMethod(cd, parts.memberName(), parts.parameterList());
				this.handleMethodDoc(md, parts, parameterName, charsAcceptor);
			}
			else
			{
				final FieldDoc fd = DocletJava8DocLink.resolveField(parts.memberName(), cd);
				this.handleFieldDoc(fd, parts, charsAcceptor);
			}
		}
		else
		{
			this.handleClassDoc(cd, parts, charsAcceptor);
		}
	}
	
	// (17.05.2019 TM)FIXME: handle referenced tags, description text, maybe multiple tags by position
		
	private void handleClassDoc(
		final ClassDoc        cd           ,
		final DocLinkTagParts parts        ,
		final CharsAcceptor   charsAcceptor
	)
	{
		// (17.05.2019 TM)FIXME: /!\ DEBUG
		if(cd.simpleTypeName().equals("Storage"))
		{
			for(final Tag tag : cd.tags())
			{
				System.err.println(tag.kind() + " " + tag.name() + ": " + tag.text());
			}
		}
		
		
		final String parsedLinkedComment = this.processDoc(cd.commentText());
		charsAcceptor.acceptChars(parsedLinkedComment);
	}
	
	private void handleFieldDoc(
		final FieldDoc        fd           ,
		final DocLinkTagParts parts        ,
		final CharsAcceptor   charsAcceptor
	)
	{
		final String parsedLinkedComment = this.processDoc(fd.commentText());
		charsAcceptor.acceptChars(parsedLinkedComment);
	}
	
	private void handleMethodDoc(
		final MethodDoc       md           ,
		final DocLinkTagParts parts        ,
		final String          parameterName,
		final CharsAcceptor   charsAcceptor
	)
	{
		
		/* (17.05.2019 TM)FIXME: additional info order
		 * order must be:
		 * 1.) if explicit parameter name is present, search for that
		 * 2.) if explicit tag name is present, search for that
		 * 3.) if explicit description marker ("!") is present, use the description
		 * 4.) else, search for the passed parameter name
		 * 5.) use the description as the default
		 */
		
		final String effectiveParameterName = UtilsDocLink.coalesce(parts.extraIdentifier(), parameterName);
		
		final ParamTag[] paramTags = md.paramTags();
		if(paramTags == null)
		{
			return;
		}
		
		for(final ParamTag paramTag : paramTags)
		{
			if(effectiveParameterName.equals(paramTag.parameterName()))
			{

				final String parsedLinkedComment = this.processDoc(paramTag.parameterComment());
				charsAcceptor.acceptChars(parsedLinkedComment);
			}
		}
	}
	
}
