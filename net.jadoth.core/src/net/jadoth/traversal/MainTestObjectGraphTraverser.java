package net.jadoth.traversal;

import net.jadoth.Jadoth;
import net.jadoth.collections.X;

public class MainTestObjectGraphTraverser
{
	public static void main(final String[] args)
	{
		final Person p1 = new Person("John", 30);
//		final Object root = X.List("a0", "b0", "c0", X.List("a1", "b1", "c1", X.List("a2", "b2", "c2", p1)));
		final Object root = X.List("a0", "b0", "c0",
			X.List(new String("a0"), new String("b0"), new String("c0"),
				X.List(new String("a0"), new String("b0"), new String("c0"), p1)
			)
		);

		final ObjectGraphTraverser obt = ObjectGraphTraverser.Factory()
//		.replaceBy(i ->
//		{
//			System.out.println(i);
//			return i instanceof String ? "" + i + "|" : i;
//		})
//		.apply(
//			String.class::isInstance,
//			System.out::println
//		)
		.where(String.class::isInstance)
		.apply(s ->
			System.out.println(Jadoth.systemString(s) + ": " + s)
		)
		.buildObjectGraphTraverser();
		
		obt.traverse(root);
		System.out.println("----");
		obt.traverse(root, TraversalDeduplicator.New(String.class));
		System.out.println("----");
		obt.traverse(root);
//
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
