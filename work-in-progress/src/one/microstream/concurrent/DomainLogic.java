package one.microstream.concurrent;

@FunctionalInterface
public interface DomainLogic<E, R>
{
	public R executeDomainLogic(E domainRootEntity);
}
