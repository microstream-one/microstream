package one.microstream.reflect;

@FunctionalInterface
public interface Getter<T, R>
{
	public R get(T instance);
}