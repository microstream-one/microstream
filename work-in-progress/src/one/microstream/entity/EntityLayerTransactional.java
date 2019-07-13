package one.microstream.entity;

public class EntityLayerTransactional
extends EntityLayer
implements EntityTransaction.Committable
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final TransactionContext transactionContext;
	private final Entity             identityLayer     ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected EntityLayerTransactional(final Entity innerLayer, final TransactionContext transactionContext)
	{
		super(innerLayer);
		this.transactionContext = transactionContext  ;
		this.identityLayer      = innerLayer.$entity();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final Entity $data()
	{
		synchronized(this.identityLayer)
		{
			return this.transactionContext.ensureData(this);
		}
	}
	
	@Override
	public final Entity actualData()
	{
		synchronized(this.$entity())
		{
			return super.$data();
		}
	}
	
	@Override
	public Entity $entity()
	{
		return this.actualData().$entity();
	}
	
	@Override
	public final boolean $updateData(final Entity newData)
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
			final Entity local = this.transactionContext.lookupData(this);
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
