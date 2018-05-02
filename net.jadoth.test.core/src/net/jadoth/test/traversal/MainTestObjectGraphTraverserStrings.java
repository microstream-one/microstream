package net.jadoth.test.traversal;

import net.jadoth.X;
import net.jadoth.traversal.Deduplicator;
import net.jadoth.traversal.ObjectGraphTraverser;
import net.jadoth.util.chars.JadothStrings;

public class MainTestObjectGraphTraverserStrings
{
	public static void main(final String[] args)
	{
		final Person p1 = new Person("Alice", 30);
		final Object root = X.List(
			"Alice",
			"Bob",
			"Charly",
			X.List(
				new String("Alice"),
				new String("Bob"),
				new String("Charly"),
				X.List(
					new String("Alice"),
					new String("Bob"),
					new String("Charly"),
					p1
				)
			)
		);
		
		final ObjectGraphTraverser stringIdPrinter = ObjectGraphTraverser.Builder()
			.root(root)
			.apply(String.class, s ->
				System.out.println(JadothStrings.systemString(s) + ": " + s)
			)
			.buildObjectGraphTraverser()
		;

		final ObjectGraphTraverser stringMutator = ObjectGraphTraverser.Builder()
			.root(root)
			.mutate(String.class, Deduplicator.New())
			.mutationListener((p, o, n) ->
			{
				System.out.println(JadothStrings.systemString(p) + "\treplacing "+o+"\t" + JadothStrings.systemString(o) + "\t-> " + JadothStrings.systemString(n));
				return false;
			})
			.buildObjectGraphTraverser()
		;
		
		System.out.println("Starting state:");
		stringIdPrinter.traverse();
		System.out.println();
		
		System.out.println("Consolidating ...");
		stringMutator.traverse();
		System.out.println();

		System.out.println("Resulting state:");
		stringIdPrinter.traverse();
		System.out.println();
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
	
	@Override
	public String toString()
	{
		return this.name + "(" + this.age + ")";
	}

}
