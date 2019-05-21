package doclink;

public interface DocLinkTagContentHandler
{
	public void handleDocLinkContent(
		char[]        input            ,
		int           start            ,
		int           bound            ,
		String        qualifiedTypeName,
		String        parameterName    ,
		CharsAcceptor charsAcceptor
	);
		
}
