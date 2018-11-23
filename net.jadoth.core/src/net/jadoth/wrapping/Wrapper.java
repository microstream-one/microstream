package net.jadoth.wrapping;


public interface Wrapper<W>
{
	public W wrapped();
	
	

	public abstract class AbstractImplementation<W> implements Wrapper<W>
	{
		private final W wrapped;

		protected AbstractImplementation(final W wrapped)
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
