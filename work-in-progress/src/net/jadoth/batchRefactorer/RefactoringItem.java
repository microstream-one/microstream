package net.jadoth.batchRefactorer;

public interface RefactoringItem
{
	public String searchPattern();
	
	public String replacementPattern();
	
	public String description();
}
