package net.jadoth.entity;

public class EntityLayerTransactional<E extends Entity<E>>
extends EntityLayer<E>
implements EntityTransaction.Committable<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final TransactionContext transactionContext;
	private final Entity<E>          identityLayer     ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected EntityLayerTransactional(final Entity<E> innerLayer, final TransactionContext transactionContext)
	{
		super(innerLayer);
		this.transactionContext = transactionContext  ;
		this.identityLayer      = innerLayer.$entity();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final E $data()
	{
		synchronized(this.identityLayer)
		{
			return this.transactionContext.ensureData(this);
		}
	}
	
	@Override
	public final E actualData()
	{
		synchronized(this.$entity())
		{
			return super.$data();
		}
	}
	
	@Override
	public E $entity()
	{
		return this.actualData().$entity();
	}
	
	@Override
	public final boolean $updateData(final E newData)
	{
		synchronized(this.$entity())
		{
			this.$validateNewData(newData);
			this.transactionContext.updateData(this, newData);
		}
		return true;
	}
	
	@Override
	public final void commit()
	{
		synchronized(this.$entity())
		{
			final E local = this.transactionContext.lookupData(this);
			if(local == null)
			{
				// no-op due to no entry at all, return.
				return;
			}
			else if(local == this.$data())
			{
				// no-op due to same instances, return.
				return;
			}

			/*
			 * set new instance unvalidated, as validation has already been done
			 * in $updateData() before registering the local data instance.
			 */
			super.$setInner(local);
		}
		
	}

	
}
