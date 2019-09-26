package one.microstream.concurrent;

// extra interface to keep the API towards the using threads clean in order to avoid confusion and errors.
public interface EnqueingDomain<E> extends Domain<E>
{
	public void enqueueTask(DomainTask<? super E, ?> task);
}
