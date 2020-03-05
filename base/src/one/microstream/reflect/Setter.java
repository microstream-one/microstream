package one.microstream.reflect;

@FunctionalInterface
public interface Setter<T, R>
{
	public void set(T instance, R reference);
}