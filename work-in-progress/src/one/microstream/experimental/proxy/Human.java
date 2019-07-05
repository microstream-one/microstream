package one.microstream.experimental.proxy;

public interface Human
{
	public String getName();

	public void doStuff();

	public void doStupidThings() throws RuntimeException;
	
	public Human say(String s);



	final class Default implements Human
	{
		private final String name;



		public Default(final String name)
		{
			super();
			this.name = name;
		}

		@Override
		public void doStuff()
		{
			System.out.println(this.name+" is thinking");
		}

		@Override
		public void doStupidThings() throws RuntimeException
		{
			System.out.println(this.name+" is trying to do stupid things... ");
			throw new RuntimeException(this.name+" broke his brain while thinking stupid things!");
		}

		@Override
		public String getName()
		{
			return this.name;
		}
		
		@Override
		public boolean equals(final Object obj)
		{
			if(obj == this)
			{
				return true;
			}
			if(obj == null || !(obj instanceof Human))
			{
				return false;
			}
			if(this.name == null)
			{
				return ((Human)obj).getName() == null;
			}
			return this.name.equals(((Human)obj).getName());
		}

		@Override
		public int hashCode()
		{
			return this.name == null ? 0 : this.name.hashCode();
		}

		@Override
		public Human say(final String s)
		{
			System.out.println(this.name+": "+s);
			return this;
		}

	}


}
