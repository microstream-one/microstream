package net.jadoth.reference;

public interface LazyReferencing<T> extends Referencing<T>
{
	/**
	 * Returns the referenced object, loading it if required.
	 * @return the lazily loaded referenced object.
	 */
	@Override
	public T get();

	/**
	 * Returns the local reference without loading the referenced object if it is not present.
	 * @return the currently present reference.
	 */
	public T peek();
}
