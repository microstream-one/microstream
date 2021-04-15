
package one.microstream.examples.helloworld;

public class DataRoot
{
	private String content;

	public DataRoot()
	{
		super();
	}

	public String getContent()
	{
		return this.content;
	}

	public void setContent(final String content)
	{
		this.content = content;
	}

	@Override
	public String toString()
	{
		return "Root: " + this.content;
	}
}
