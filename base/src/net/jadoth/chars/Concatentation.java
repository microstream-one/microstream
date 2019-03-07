/**
 *
 */
package net.jadoth.chars;

/**
 * @author Thomas Muenz
 *
 */
public class Concatentation implements CharSequence
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final CharSequence s1;
	private final Concatentation c1;

	private final CharSequence s2;
	private final Concatentation c2;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public Concatentation(final CharSequence s1, final CharSequence s2)
	{
		super();
		this.s1 = s1;
		this.c1 = s1 instanceof Concatentation ? (Concatentation)s1 : null;
		this.s2 = s2;
		this.c2 = s2 instanceof Concatentation ? (Concatentation)s2 : null;
	}


	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return this.assemble(new StringBuilder(this.length())).toString();
	}
	/**
	 * @param index
	 * @return
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@SuppressWarnings("null") //invalid potential null pointer warning
	@Override
	public char charAt(final int index)
	{
		final CharSequence s1 = this.s1;
		final int s1Len = s1 != null ? s1.length() : 0;
		if(s1Len > index)
		{
			return s1.charAt(index);
		}

		final CharSequence s2 = this.s2;
		final int s2Len = s2 != null ? s2.length() : 0;
		if(s2Len <= index)
		{
			throw new StringIndexOutOfBoundsException(index + " not in " + (s1Len + s2Len));
		}
		return s2.charAt(index - s1Len);
	}
	/**
	 * @return
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length()
	{
		return
			(this.s1 != null  ? this.s1.length() : 0) + (this.s2 != null  ? this.s2.length() : 0)
		;
	}

	/**
	 * Assembles a String resembling this concatenation and builds a String subSequence from it.
	 *
	 * @param start the start index of the sub sequence.
	 * @param end the end index of the sub sequence.
	 * @return a String containing the defined sub sequence.
	 * @see java.lang.CharSequence#subSequence(int, int)
	 * @see java.lang.String#subSequence(int, int)
	 */
	@Override
	public String subSequence(final int start, final int end)
	{
		return this.toString().subSequence(start, end).toString();
	}


	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public StringBuilder assemble(final StringBuilder sb)
	{
		if(this.c1 != null)
		{
			this.c1.assemble(sb);
		}
		else
		{
			sb.append(this.s1);
		}
		if(this.c2 != null)
		{
			this.c2.assemble(sb);
		}
		else
		{
			sb.append(this.s2);
		}

		return sb;
	}

}
