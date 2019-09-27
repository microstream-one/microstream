package one.microstream.concurrent;

public interface Domain<E>
{
	public <R> R executeLogic(final DomainLogic<? super E, R> logic);
		
}

