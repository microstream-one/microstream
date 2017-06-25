package net.jadoth.traversal2;

public interface ReferenceAccessor<T>
{
	public T get(Object parent);
	
	public T set(Object parent, T newValue);
}
