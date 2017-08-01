package net.jadoth.traversal;

import net.jadoth.Jadoth;
import net.jadoth.collections.X;

public class MainTestObjectGraphTraverser
{
	public static void main(final String[] args)
	{
		final Person p1 = new Person("John", 30);
		final Object root = X.List("a0", "b0", "c0",
			X.List(new String("a0"), new String("b0"), new String("c0"),
				X.List(new String("a0"), new String("b0"), new String("c0"), p1)
			)
		);
		
		final ObjectGraphTraverser stringIdPrinter = ObjectGraphTraverser.Builder()
			.from(root)
			.select(String.class::isInstance)
			.apply(s ->
				System.out.println(Jadoth.systemString(s) + ": " + s)
			)
			.buildObjectGraphTraverser()
		;

		final ObjectGraphTraverser stringMutator = ObjectGraphTraverser.Builder()
			.from(root)
			.select(String.class::isInstance)
			.mutateBy(Deduplicator.New())
			.buildObjectGraphTraverser()
		;
		
		stringIdPrinter.traverse();
		System.out.println("----");
		stringMutator.traverse();
		stringIdPrinter.traverse();
	}
}


final class Person
{
	String name;
	int    age ;
	
	public Person(final String name, final int age)
	{
		this.name = name;
		this.age = age;
	}

}
