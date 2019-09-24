package one.microstream.concurrent;

@FunctionalInterface
public interface DomainLookup<A, E>
{
	public Domain<E> lookupDomain(A applicationRoot);
}
