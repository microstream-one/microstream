package one.microstream.util.traversing;

public interface TraversalMode
{
	public void handle(Object[] instances, TraversalReferenceHandler referenceHandler);
	
	
	public final class Full implements TraversalMode
	{
		@Override
		public final void handle(final Object[] instances, final TraversalReferenceHandler referenceHandler)
		{
			referenceHandler.handleAsFull(instances);
		}
		
	}
	
	public final class Node implements TraversalMode
	{
		@Override
		public final void handle(final Object[] instances, final TraversalReferenceHandler referenceHandler)
		{
			referenceHandler.handleAsNode(instances);
		}
		
	}
	
	public final class Leaf implements TraversalMode
	{
		@Override
		public final void handle(final Object[] instances, final TraversalReferenceHandler referenceHandler)
		{
			referenceHandler.handleAsLeaf(instances);
		}
		
	}
}
