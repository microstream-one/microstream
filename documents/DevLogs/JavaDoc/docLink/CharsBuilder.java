package doclink;

public interface CharsBuilder extends CharsAcceptor
{
	public default CharsBuilder prepare()
	{
		this.reset();
		return this;
	}
	
	public CharsBuilder reset();
	
	public String yield();
	
	
	
	public static CharsBuilder New()
	{
		return New(new StringBuilder());
	}
	
	public static CharsBuilder New(final StringBuilder sb)
	{
		return new CharsBuilder.Default(
			UtilsDocLink.notNull(sb)
		);
	}
	
	public final class Default implements CharsBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StringBuilder sb;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final StringBuilder sb)
		{
			super();
			this.sb = sb;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void acceptChar(final char c)
		{
			this.sb.append(c);
		}

		@Override
		public void acceptChars(final String input)
		{
			this.sb.append(input);
		}
		
		@Override
		public void acceptChars(final CharSequence input, final int offset, final int length)
		{
			this.sb.append(input, offset, offset + length);
		}
		
		@Override
		public void acceptChars(final char[] input, final int offset, final int length)
		{
			this.sb.append(input, offset, length);
		}

		@Override
		public CharsBuilder reset()
		{
			UtilsDocLink.clear(this.sb);
			return this;
		}

		@Override
		public String yield()
		{
			return this.sb.toString();
		}
		
	}
	
}
