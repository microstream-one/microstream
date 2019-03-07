package net.jadoth.experimental.basic;


public interface TypeB
{
	public Object getBValue();
	public void setBValue(Object aValue);
	public String whoAmI();


	public class Implementation implements TypeB
	{
		protected Object bValue = null;
		protected Object protectedBValue = null;


		@Override
		public Object getBValue()
		{
			return this.bValue;
		}

		@Override
		public void setBValue(final Object aValue)
		{
			this.bValue = aValue;
		}

		@Override
		public String toString()
		{
			return this.whoAmI();
		}

		@Override
		public String whoAmI()
		{
			System.out.println("executing TypeB.Implementation.whoAmI()");
			return getInternWhoAmI();
		}

		protected String getInternWhoAmI()
		{
			return "I'm TypeB.Implementation";
		}

	}

}
