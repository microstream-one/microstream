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
		this.identityLayer      = Entity.identity(innerLayer);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	protected final Entity entityData()
	{
		synchronized(this.identityLayer)
		{
			return this.transactionContext.ensureData(this);
		}
	}
	
	@Override
	public final Entity actualData()
	{
		synchronized(this.entityIdentity())
		{
			return super.entityData();
		}
	}
	
	@Override
	protected Entity entityIdentity()
	{
		return Entity.identity(this.actualData());
	}
	
	@Override
	protected final boolean updateEntityData(final Entity newData)
	{
		synchronized(this.entityIdentity())
		{
			this.$validateNewData(newData);
			this.transactionContext.updateData(this, newData);
		}
		return true;
	}
	
	@Override
	public final void commit()
	{
		synchronized(this.entityIdentity())
		{
			final Entity local = this.transactionContext.lookupData(this);
			if(local == null)
			{
				// no-op due to no entry at all, return.
				return;
			}
			else if(local == this.entityData())
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
