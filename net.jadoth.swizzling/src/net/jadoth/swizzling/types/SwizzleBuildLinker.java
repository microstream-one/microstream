package net.jadoth.swizzling.types;

public interface SwizzleBuildLinker extends SwizzleObjectIdResolving
{
	public SwizzleObjectSupplier getSwizzleObjectSupplier();


	/**
	 * Helper instances can be used as temporary additional state for the duration of the building process.
	 * E.g.: JDK hash collections cannot properly collect elements during the building process as the element instances
	 * might still be in an initialized state without their proper data, so hashing and equality comparisons would
	 * failt or result in all elements being "equal". So building JDK hash collections required to pre-collect
	 * their elements in an additional helper structure and defer the actual elements collecting to the completion.
	 * <p>
	 * Similar problems with other or complex custom handlers are conceivable.
	 *<p>
	 * Only one helper object can be registered per subject instance (the instance to be built).
	 *
	 * @param subject
	 * @param helper
	 * @return
	 */
	public boolean registerHelper(Object subject, Object helper);

	/**
	 * Helper instances can be used as temporary additional state for the duration of the building process.
	 * E.g.: JDK hash collections cannot properly collect elements during the building process as the element instances
	 * might still be in an initialized state without their proper data, so hashing and equality comparisons would
	 * failt or result in all elements being "equal". So building JDK hash collections required to pre-collect
	 * their elements in an additional helper structure and defer the actual elements collecting to the completion.
	 * <p>
	 * Similar problems with other or complex custom handlers are conceivable.
	 *<p>
	 * Only one helper object can be registered per subject instance (the instance to be built).
	 *
	 * @param subject
	 * @return
	 */
	public Object getHelper(Object subject);

	/**
	 * Helper instances can be used as temporary additional state for the duration of the building process.
	 * E.g.: JDK hash collections cannot properly collect elements during the building process as the element instances
	 * might still be in an initialized state without their proper data, so hashing and equality comparisons would
	 * failt or result in all elements being "equal". So building JDK hash collections required to pre-collect
	 * their elements in an additional helper structure and defer the actual elements collecting to the completion.
	 * <p>
	 * Similar problems with other or complex custom handlers are conceivable.
	 *<p>
	 * Only one helper object can be registered per subject instance (the instance to be built).
	 *
	 * @param subject
	 * @return
	 */
	public Object removeHelper(Object subject);

}
