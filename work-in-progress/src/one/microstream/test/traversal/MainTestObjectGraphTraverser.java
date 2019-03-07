package one.microstream.test.traversal;

import one.microstream.X;
import one.microstream.util.traversing.ObjectGraphTraverser;

public class MainTestObjectGraphTraverser
{
	public static void main(final String[] args)
	{
		final Object root = X.List(
			X.Enum(
				new Person("Alice", 30)
			),
			X.Enum(
				new Person("Bob", 30)
			)
		);
		
		final ObjectGraphTraverser printer = ObjectGraphTraverser.Builder()
			.root(root)
			.apply(i -> System.out.println(i.getClass().getSimpleName() + ": " + i))
			.buildObjectGraphTraverser()
		;
		printer.traverse();
	}
}
