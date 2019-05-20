package doclink;

public interface DocLinkTagContentHandler
{
	public void handleDocLinkContent(
		char[]        input        ,
		int           start        ,
		int           bound        ,
		String        parameterName,
		CharsAcceptor charsAcceptor
	);
		
}
