package doclink.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;

import doclink.CharsAcceptor;
import doclink.DocLink;
import doclink.DocLinkTagParts;
import doclink.DocLinker;
import doclink.UtilsDocLink;

/**
 * Implementation of {@link DocLinker} used by {@literal com.sun.javadoc.Doclet} pseudo-implementors
 * like {@literal DocletJava8DocLinkPrinter}.
 * 
 * @author TM
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
		final DocLinkTagParts parts            ,
		final String          qualifiedTypeName,
		final String          parameterName    ,
		final CharsAcceptor   charsAcceptor
	)
	{
		final ClassDoc cd = DocletJava8DocLink.resolveClass(parts.typeName(), this.docRoot);
		
		if(parts.isMember())
		{
			if(parts.isMethod())
			{
//				System.out.println("Resolve Method " + cd.qualifiedName() + "#" + parts.memberName() + Arrays.toString(parts.parameterList()));
				final MethodDoc md = DocletJava8DocLink.resolveMethod(cd, parts.memberName(), parts.parameterList());
//				System.out.println("md = " + md);
				this.handleMethodDoc(qualifiedTypeName, md, parts, parameterName, charsAcceptor);
			}
			else
			{
//				System.out.println("Resolve Field " + cd.qualifiedName() + "#" + parts.memberName());
				final FieldDoc fd = DocletJava8DocLink.resolveField(parts.memberName(), cd);
				this.handleFieldDoc(qualifiedTypeName, fd, parts, charsAcceptor);
			}
		}
		else
		{
			this.handleClassDoc(qualifiedTypeName, cd, parts, charsAcceptor);
		}
	}
			
	private void handleClassDoc(
		final String          qualifiedTypeName,
		final ClassDoc        cd               ,
		final DocLinkTagParts parts            ,
		final CharsAcceptor   charsAcceptor
	)
	{
		if(cd == null)
		{
			return;
		}
		
		if(parts.tagName() != null)
		{
			this.handleByTag(
				qualifiedTypeName,
				cd.qualifiedName(),
				cd.tags(),
				parts.tagName(),
				parts.extraIdentifier(),
				charsAcceptor
			);
			return;
		}
		
		this.useComment(charsAcceptor, cd.commentText());
	}
	
	private void handleFieldDoc(
		final String          qualifiedTypeName,
		final FieldDoc        fd               ,
		final DocLinkTagParts parts            ,
		final CharsAcceptor   charsAcceptor
	)
	{
		if(fd == null)
		{
			return;
		}
		
		if(parts.tagName() != null)
		{
			this.handleByTag(
				qualifiedTypeName,
				fd.qualifiedName(),
				fd.tags(),
				parts.tagName(),
				parts.extraIdentifier(),
				charsAcceptor
			);
			return;
		}
		
//		System.out.println("Field " + fd.qualifiedName()+ ":\n");
//		System.out.println("getRawCommentText:\n" + fd.getRawCommentText());
//		System.out.println("commentText:\n" + fd.commentText());
//		System.out.println("tags:\n");
//		for(final Tag tag : UtilsDocLink.nonNull(fd.tags()))
//		{
//			System.out.println(tag.name());
//		}
		
		this.useComment(charsAcceptor, fd.commentText());
	}
	
	private void handleMethodDoc(
		final String          qualifiedTypeName,
		final MethodDoc       md               ,
		final DocLinkTagParts parts            ,
		final String          parameterName    ,
		final CharsAcceptor   charsAcceptor
	)
	{
		if(md == null)
		{
			return;
		}
		
//		System.out.println("handleMethodDoc:\n" + DocLinkTagDebugger.toDebugString(parts, parameterName));
		
		// priority 1: explicit tag takes precedence.
		if(parts.tagName() != null)
		{
			this.handleMethodDocByTag(md, parts, qualifiedTypeName, parameterName, charsAcceptor);
			return;
		}
		
		// priority 2: explicit identifier
		if(parts.extraIdentifier() != null)
		{
			// use current parameter tag's parameter name (null in all other cases) or explicit parameter name
			final String effParamName = DocLink.determineEffectiveParameterName(parameterName, parts.extraIdentifier());
			this.handleMethodDocByParameterName(md, effParamName, charsAcceptor);
			return;
		}
		
		// fallback/default: the method's general description (comment text) is used.
		this.handleDocGenerically(md, charsAcceptor);
	}
		
	private void handleDocGenerically(
		final Doc           doc          ,
		final CharsAcceptor charsAcceptor
	)
	{
		// (20.05.2019 TM)FIXME: test and comment if correct
		this.useComment(charsAcceptor, doc.commentText());
	}
	
	private void useComment(
		final CharsAcceptor charsAcceptor,
		final String        rawComment
	)
	{
		final String parsedLinkedComment = this.processDoc(rawComment);
		charsAcceptor.acceptChars(parsedLinkedComment);
	}
	
	private void handleProblem(final String problem)
	{
		throw new RuntimeException(problem);
	}
	
	private void handleMethodDocByTag(
		final MethodDoc       md               ,
		final DocLinkTagParts parts            ,
		final String          qualifiedTypeName,
		final String          parameterName    ,
		final CharsAcceptor   charsAcceptor
	)
	{
		if("param".equals(parts.tagName()))
		{
//			System.out.println("param:\n" + DocLinkTagDebugger.toDebugString(parts, parameterName));
			
			final ParamTag paramTag = DocletJava8DocLink.searchParamTag(
				md.paramTags(),
				parameterName,
				parts.extraIdentifier()
			);
			if(paramTag != null)
			{
				this.useComment(charsAcceptor, paramTag.parameterComment());
				return;
			}
		}

		this.handleByTag(qualifiedTypeName, md.qualifiedName(), md.tags(), parts.tagName(), parts.extraIdentifier(), charsAcceptor);
	}
	
	private void handleByTag(
		final String        qualifiedTypeName   ,
		final String        qualifiedSubjectName,
		final Tag[]         tags                ,
		final String        tagName             ,
		final String        extraIdentifier     ,
		final CharsAcceptor charsAcceptor
	)
	{
		final Tag tag = DocletJava8DocLink.searchNonParamTag(
			tags,
			tagName,
			extraIdentifier
		);
		if(tag != null)
		{
			this.useTag(charsAcceptor, qualifiedTypeName, tag);
			return;
		}
		
		this.handleProblem("No tag found with name \"" + tagName + "\" for " + qualifiedSubjectName);
	}
	
	private void useTag(
		final CharsAcceptor charsAcceptor    ,
		final String        qualifiedTypeName,
		final Tag           tag
	)
	{
		if(tag instanceof SeeTag)
		{
			this.useSeeTag(charsAcceptor, qualifiedTypeName, (SeeTag)tag);
			return;
		}
		
		this.useComment(charsAcceptor, tag.text());
	}
	
	/**
	 * Special casing to transform local to global type identifiers if necessary.
	 * 
	 * @param charsAcceptor
	 * @param fullQualifiedTypeName
	 * @param seetag
	 */
	private void useSeeTag(
		final CharsAcceptor charsAcceptor    ,
		final String        qualifiedTypeName,
		final SeeTag        seeTag
	)
	{
		final String tagOriginalText = seeTag.text();
		final String commentToBeUsed;
		if(seeTag.referencedClass().qualifiedName().equals(qualifiedTypeName)
			|| tagOriginalText.indexOf('#') < 0
		)
		{
			commentToBeUsed = tagOriginalText;
		}
		else
		{
			commentToBeUsed = seeTag.referencedClass().qualifiedName()
				+ tagOriginalText.substring(tagOriginalText.indexOf('#'))
			;
		}
		
		this.useComment(charsAcceptor, commentToBeUsed);
	}
		
	private void handleMethodDocByParameterName(
		final MethodDoc       md           ,
		final String          parameterName,
		final CharsAcceptor   charsAcceptor
	)
	{
		final ParamTag[] paramTags = md.paramTags();
		if(paramTags == null)
		{
			if(parameterName != null)
			{
				this.handleProblem(
					"No parameters found to look up parameter \"" + parameterName
					+ "\" for method " + md.qualifiedName()
				);
			}
			return;
		}
		
		for(final ParamTag paramTag : paramTags)
		{
			if(paramTag.parameterName().equals(parameterName))
			{
				this.useComment(charsAcceptor, paramTag.parameterComment());
				return;
			}
		}

		this.handleProblem("No parameter found with name \"" + parameterName + "\" for method " + md.qualifiedName());
	}
	
}
