package net.jadoth.persistence.types;

public interface PersistenceChannel<M> extends PersistenceTarget<M>, PersistenceSource<M>
{
	// just a typing interface so far.
	
	public default void prepareChannel()
	{
		this.prepareSource();
		this.prepareTarget();
	}
	
	public default void closeChannel()
	{
		this.closeSource();
		this.closeTarget();
	}
	
}
