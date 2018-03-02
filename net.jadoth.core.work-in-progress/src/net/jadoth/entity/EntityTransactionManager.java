package net.jadoth.entity;

public interface EntityTransactionManager<T> extends TransactionContext
{
	public EntityTransaction getCurrent();
	
	public EntityTransaction ensureTransaction(T token);
	
	public EntityTransaction lookupTransaction(T token);
	
	public EntityTransaction removeTransaction(T token);
}
