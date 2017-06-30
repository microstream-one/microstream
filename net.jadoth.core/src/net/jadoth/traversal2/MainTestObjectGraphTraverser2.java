package net.jadoth.traversal2;

import net.jadoth.collections.X;

public class MainTestObjectGraphTraverser2
{
	public static void main(final String[] args)
	{
		ObjectGraphTraverser2.Factory().buildObjectGraphTraverser().traverse(
			X.List("a", "b", "c", "d"),
			(i, p, e) ->
			{
				System.out.println(i);
				return i;
			}
		);
	}
}
