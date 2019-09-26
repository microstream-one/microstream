package one.microstream.concurrent;


public interface DomainTaskCreator
{
	public <E, R> DomainTask<E, R> createDomainTask(
		Domain<E>                 domain     ,
		DomainLogic<? super E, R> linkedLogic
	);
}
