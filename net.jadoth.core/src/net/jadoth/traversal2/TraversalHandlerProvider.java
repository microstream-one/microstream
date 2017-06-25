package net.jadoth.traversal2;

public interface TraversalHandlerProvider
{
	public <T> TraversalHandler<T> provideTraversalHandler(T instance);
	
	/* (25.06.2017 TM)FIXME: provideTraversalHandler
	 *  - lookup per instance
	 *  - lookup per exact type
	 *  - lookup per polymorph type
	 */
}
