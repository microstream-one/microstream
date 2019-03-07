package one.microstream.experimental.aspectorientation.test;

import one.microstream.experimental.aspectorientation.MethodCall;

/**
 *
 */

/**
 * @author Thomas Muenz
 *
 */
public class MyClass
{
	public StringBuilder doStuff(final int i, final float f, final String s) throws NullPointerException {
		//MethodExecutionContext and even the MethodCall could be cached, of course
		return MyAspects.getMethodExecutionContext(this).executeMethod(new doStuff(i, f, s));
	}

	//Method class could be extended by subclass of MyClass by additional fields, references, etc
	//execute() could as well relay back to a MyClass-Method, like "private _execute$doStuff()" or so
	protected class doStuff extends MethodCall<StringBuilder>
	{
		final protected int i;
		final protected float f;
		final protected String s;

		protected doStuff(final int i, final float f, final String s)
		{
			this.i = i;
			this.f = f;
			this.s = s;
		}

		@Override
		public StringBuilder execute() throws NullPointerException
		{
			if(this.s == null)
			{
				throw new NullPointerException("s may not be null");
			}
			final StringBuilder sb = new StringBuilder(1024);
			sb.append("i is "+this.i+", f is "+this.f+", s is \""+this.s+"\"");
			return sb;
		}
	}

}
