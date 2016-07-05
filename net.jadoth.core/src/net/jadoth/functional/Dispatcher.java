package net.jadoth.functional;

public interface Dispatcher
{
	public <T> T apply(T subject);
}
