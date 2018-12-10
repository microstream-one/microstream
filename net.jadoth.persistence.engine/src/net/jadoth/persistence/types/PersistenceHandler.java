package net.jadoth.persistence.types;

public interface PersistenceHandler extends PersistenceFunction
{
	/**
	 * The "natural" way of handling an instance as defined by the implementation.
	 */
	@Override
	public <T> long apply(T instance);
	
	/**
	 * A way to signal to the implementation that the passed instance is supposed to be handled eagerly,
	 * meaning it shall be handled even if the handling implementation does not deem it necessary.<br>
	 * This is needed, for example, to store composition pattern instances without breaking OOP encapsulation concepts.
	 */
	public <T> long applyEager(T instance);
	
	public PersistenceObjectRetriever getObjectRetriever();
	
}
