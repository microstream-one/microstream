package one.microstream.util.traversing;

public interface TraversalEnqueuer
{
	public boolean skip(Object instance);
	
	public void enqueue(Object instance);
	
	
	
	/* (23.08.2017 TM)FIXME: enqueue stack with proper order
	 * Implementing the enqueuing with a FiFo list as it done currently causes a memory overflow for larger graphs
	 * or generally an inefficient memory usage. The FiFo list first enqueues all the graph's reference
	 * subsequentially and then starts processing the gigantic list from start to end.
	 * It would be better if the handling of one instance would prepend its references, so that they get processed
	 * next.
	 * But a strict prepending would inverse the order of the references, which could cause erroneous bahavior
	 * if the user logic expects the order to be proper.
	 * The solution is:
	 * The traversing of one instance does not directly enqueue its references, but only "prepares" to enqueue them
	 * in proper order. After all of the current instance's references have been "prepareEnqueued", the prepared
	 * references are committed the the actual queue.
	 * The commit itself just means to connect the internal buffer segments.
	 * 
	 * Maybe this can be avoided by a smarter algorithm:
	 * - Track the "current dequeue" segment.
	 * - Enqueue in order always in the segment BEFORE the current dequeue segment, if that is full, insert a new one
	 * - inside dequeue(), always use the earliest segment in the chain.
	 * - if the tail dummy segment is reached, the traversal is completed.
	 * 
	 */
	
//	public void prepareEnqueue(Object instance);
//
//	public void commitEnqueue();
		
}
