package doclink;

public final class UtilsDocLink
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	// (15.05.2019 TM)TODO: Should be replaced by using the microstream base project API
	
	public static final <T> T notNull(final T object) throws NullPointerException
	{
		if(object == null)
		{
			// removing this method's stack trace entry is kind of a hack. On the other hand, it's not.
			throw new NullPointerException();
		}
		return object;
	}
	
	public static final <T> T coalesce(final T firstElement, final T secondElement)
	{
		return firstElement == null
			? secondElement
			: firstElement
		;
	}

	static final int indexOf(final char[] input, final int start, final int bound, final char c)
	{
		for(int i = start; i < bound; i++)
		{
			if(input[i] == c)
			{
				return i;
			}
		}
		return -1;
	}
	
	static final boolean equalsCharSequence(final char[] input, final int i, final char[] sample)
	{
		if(input.length - i < sample.length)
		{
			return false;
		}
		
		for(int s = 0; s < sample.length; s++)
		{
			if(input[i + s] != sample[s])
			{
				return false;
			}
		}
		return true;
	}
	
	static final int skipWhiteSpaces(final char[] input, int i, final int bound)
	{
		while(i < bound && input[i] <= ' ')
		{
			i++;
		}
		return i;
	}
	
	static final int trimWhiteSpaces(final char[] input, final int start, final int bound)
	{
		int i = bound - 1;
		while(i >= start && input[i] <= ' ')
		{
			i--;
		}
		
		return i + 1;
	}
	
	static final int skipToChar(final char[] input, final int start, final int bound, final char c)
	{
		for(int i = start; i < bound; i++)
		{
			if(input[i] == c)
			{
				return i;
			}
		}
		
		// char not found, nowhere to skip to
		return start;
	}
	
	public static StringBuilder clear(final StringBuilder sb)
	{
		// because a clear() or equivalent was too hard to implement for them ...
		sb.setLength(0);
		return sb;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private UtilsDocLink()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
