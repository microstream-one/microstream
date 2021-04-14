package one.microstream.wrapping;

/**
 * Generic interface for the wrapper (decorator) pattern.
 * 
 * , FH
 */
public interface Wrapper<W>
{
	public W wrapped();
	
	
	public abstract class Abstract<W> implements Wrapper<W>
	{
		private final W wrapped;

		protected Abstract(final W wrapped)
		{
			super();
			
			this.wrapped = wrapped;
		}
		
		@Override
		public final W wrapped()
		{
			return this.wrapped;
		}
		
	}
	
}
