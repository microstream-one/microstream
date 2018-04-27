package net.jadoth.swizzling.types;

public interface PersistenceStoreFunction extends SwizzleFunction
{
	/**
	 * The "natural" way of storin an instance as defined by the implementation.
	 */
	@Override
	public <T> long apply(T instance);
	
	/**
	 * A way to signal to the implementation that the passed instance is supposed to be stored eagerly,
	 * meaning it shall be stored even if the storing implementation does not deem it necessary.<br>
	 * This is needed, for example, to store composition pattern instances without breaking encapsulation.
	 */
	public <T> long applyEager(T instance);
	
	public SwizzleObjectSupplier getSwizzleObjectSupplier();
	
}
