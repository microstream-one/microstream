package one.microstream.experimental.basic;


public interface TypeA
{
	public Object getAValue();
	public void setAValue(Object aValue);
	public String whoAmI();


	public class Implementation implements TypeA
	{
		protected Object aValue = null;
		protected Object protectedAValue = null;

		@Override
		public Object getAValue()
		{
			return this.aValue;
		}

		@Override
		public void setAValue(final Object aValue)
		{
			this.aValue = aValue;
		}

		@Override
		public String toString()
		{
			return this.whoAmI();
		}

		@Override
		public String whoAmI()
		{
			System.out.println("executing TypeA.Implementation.whoAmI()");
			return getInternWhoAmI();
		}

		protected String getInternWhoAmI()
		{
			return "I'm TypeA.Implementation";
		}
	}

}
