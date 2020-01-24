package one.microstream.test.basic;

class AppRoot<T>
{
	T value;
	
	AppRoot<T> set(final T value)
	{
		this.value = value;
		
		return this;
	}
	
	@Override
	public String toString()
	{
		return super.toString() + " value = " + this.value;
	}
	
}