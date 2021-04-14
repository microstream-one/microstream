package one.microstream.persistence.types;

public interface PersistenceChannel<D> extends PersistenceTarget<D>, PersistenceSource<D>
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
