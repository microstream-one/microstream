package net.jadoth.entity;

import net.jadoth.collections.HashTable;
import net.jadoth.entity.EntityTransaction.DataConflict;

public class EntityTransactionExceptionDataConflict extends EntityTransactionException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final HashTable<Entity<?>, DataConflict<?>> conflicts;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public EntityTransactionExceptionDataConflict(final HashTable<Entity<?>, DataConflict<?>> conflicts)
	{
		super();
		this.conflicts = conflicts;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public HashTable<Entity<?>, DataConflict<?>> conflicts()
	{
		return this.conflicts;
	}
	
}
