package one.microstream.batchRefactorer;

@FunctionalInterface
public interface PatternNormalizer
{
	public String normalizeSearchPattern(String pattern);
	
	public default String normalizeReplacementPattern(final String pattern)
	{
		return this.normalizeSearchPattern(pattern);
	}
	
}
