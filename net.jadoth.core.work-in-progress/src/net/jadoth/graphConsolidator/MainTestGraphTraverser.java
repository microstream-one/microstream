package net.jadoth.graphConsolidator;

import net.jadoth.Jadoth;
import net.jadoth.traversal.ObjectGraphTraverser;

public class MainTestGraphTraverser
{
	public static void main(final String[] args)
	{
		ObjectGraphTraverser.traverseGraph(null, e -> System.out.println(Jadoth.systemString(e)));
	}
}
