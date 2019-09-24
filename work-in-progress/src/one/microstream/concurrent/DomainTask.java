package one.microstream.concurrent;

public interface DomainTask<R>
{
	public R result();
}
