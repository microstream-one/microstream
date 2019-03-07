package one.microstream.network.types;


public interface NetworkServer extends Suspendable
{
	public boolean isShutdown();

	public boolean isActivating();

	public boolean isDeactivating();

}
