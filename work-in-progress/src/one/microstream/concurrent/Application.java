package one.microstream.concurrent;

public interface Application<A>
{
	// (24.09.2019 TM)TODO: not sure this is needed at all. Maybe for special cases and/or controlling/debugging.
	public <E> E getDomainRootEntity(DomainLookup<? super A, E> lookup);
	
	// (24.09.2019 TM)NOTE: ideally, this should be the sole method in the whole type
	public void executeTask(ApplicationTask<? super A> task);
	
}
