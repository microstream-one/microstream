package one.microstream.batchRefactorer;

public interface LineBreakStrategy
{
	public String lineBreak();
		
	public String restore(String normalized);
	
	
	public final class CrLf implements LineBreakStrategy
	{

		@Override
		public final String lineBreak()
		{
			return "\r\n";
		}

		@Override
		public final String restore(final String normalized)
		{
			return normalized.replaceAll("\\n", "\r\n");
		}
		
	}
	
	public final class Cr implements LineBreakStrategy
	{

		@Override
		public final String lineBreak()
		{
			return "\r";
		}

		@Override
		public final String restore(final String normalized)
		{
			return normalized.replaceAll("\\n", "\r");
		}
		
	}
	
	public final class Lf implements LineBreakStrategy
	{

		@Override
		public final String lineBreak()
		{
			return "\n";
		}

		@Override
		public final String restore(final String normalized)
		{
			return normalized;
		}
		
	}
	
}
