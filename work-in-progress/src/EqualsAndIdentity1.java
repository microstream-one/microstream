public class EqualsAndIdentity1
{

	public static void main(final String[] args)
	{
		final Person p1 = new Person("John", 30);
		final Person p2 = new Person("John", 30);
		
		System.out.println("p1("+p1+")   ==   p2("+p2+"): "+(p1 == p2));
		System.out.println("p1("+p1+") equals p2("+p2+"): "+(p1.equals(p2)));

	}	
	
}

class Person
{
	String name;
	int age;
	
	public Person(final String name, final int age)
	{
		this.name = name;
		this.age = age;
	}
	
}
