
package one.microstream.storage.restclient.app;

public class SessionData
{
	private final String baseUrl;
	
	public SessionData(
		final String baseUrl
	)
	{
		super();
		this.baseUrl = baseUrl;
	}
	
	public String baseUrl()
	{
		return this.baseUrl;
	}
}
