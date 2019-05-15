package doclink;

public interface DocLinkTagProcessor
{
	public void signalTagStart(char[] chars, int index);
	
	public void processDocLinkContent(char[] chars, int offset, int bound);
	
	public void signalTagEnd(char[] chars, int index);
	
	public void signalInputEnd(char[] chars, int bound);
	
}
