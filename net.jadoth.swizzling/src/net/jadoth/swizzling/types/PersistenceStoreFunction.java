package net.jadoth.swizzling.types;

public interface PersistenceStoreFunction extends SwizzleFunction
{
	/**
	 * The "natural" to store an instance. The actual strategy is decided by the implementation.
	 */
	@Override
	public <T> long apply(T instance);
	
	/**
	 * A way to override the implementation logic used in {@link #apply(Object)} and force the storer to
	 * store the instance. Is is necessary if the TypeHandler decides that a certain field or instance must
	 * be stored in any case, e.g. to properly handle compositions or fields marked as "mandatory" to be stored.
	 */
	public <T> long applyForced(T instance);
	
	public SwizzleObjectSupplier getSwizzleObjectSupplier();
	
}
