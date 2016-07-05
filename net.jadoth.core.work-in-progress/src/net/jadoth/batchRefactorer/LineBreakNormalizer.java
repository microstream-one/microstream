package net.jadoth.batchRefactorer;


@FunctionalInterface
public interface LineBreakNormalizer
{
	public String normalize(final String s);
	
	
	public final class Lf implements LineBreakNormalizer
	{

		@Override
		public final String normalize(final String s)
		{
			return s.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
		}
		
	}
}
