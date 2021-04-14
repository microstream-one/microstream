package one.microstream.persistence.types;

import one.microstream.reference.ObjectSwizzling;

public interface Persister extends ObjectSwizzling, PersistenceStoring
{
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getObject(long objectId);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long store(Object instance);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long[] storeAll(Object... instances);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void storeAll(Iterable<?> instances);

	
	/**
	 * Creates a new {@link Storer} instance with lazy storing behavior. This means an entity instance encountered
	 * while traversing the entity graph is only stored if it is not yet known to the persistence context, i.e.
	 * does not have an objectId associated with it in the persistence context's {@link PersistenceObjectRegistry}.
	 * 
	 * @return the newly created {@link Storer} instance.
	 */
	public Storer createLazyStorer();
	
	/**
	 * Creates a new {@link Storer} instance with default storing behavior. The default is lazy storing.
	 * See {@link #createLazyStorer()}.
	 * 
	 * @return the newly created {@link Storer} instance.
	 */
	public Storer createStorer();

	/**
	 * Creates a new {@link Storer} instance with eager storing behavior. This means an entity instance encountered
	 * while traversing the entity graph is always stored, regardless of if it is already known to the persistence
	 * context or not, i.e. does have an objectId associated with it in the persistence context's
	 * {@link PersistenceObjectRegistry}.
	 * <p>
	 * Note: Eager storing is a dangerous behavior since - depending on the entity graph's referential layout -
	 * it can cause the whole entity graph present in the heap to be stored. Therefore, it is stronly advised to
	 * instead use lazy storing logic (see {@link #createLazyStorer()}) or some other kind of limiting storing logic.
	 * 
	 * @return the newly created {@link Storer} instance.
	 */
	public Storer createEagerStorer();
	
}
