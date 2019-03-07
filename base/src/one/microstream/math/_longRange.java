package one.microstream.math;

public interface _longRange
{
	public long start();
	
	public long bound();
	
	public default long length()
	{
		return this.bound() - this.start();
	}
	
	
	
	public static _longRange New(final long start, final long bound)
	{
		return new _longRange.Implementation(start, bound);
	}
	
	public final class Implementation implements _longRange
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long start;
		private final long bound;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final long start, final long bound)
		{
			super();
			this.start = start;
			this.bound = bound;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final long start()
		{
			return this.start;
		}
		
		@Override
		public final long bound()
		{
			return this.bound;
		}
		
	}
	
}
