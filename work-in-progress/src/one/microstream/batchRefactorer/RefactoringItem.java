package one.microstream.batchRefactorer;

public interface RefactoringItem
{
	public String searchPattern();
	
	public String replacementPattern();
	
	public String description();
}
