package one.microstream.viewer.server;

import one.microstream.viewer.StorageRestAdapter;
import spark.Route;

public abstract class RouteBase implements Route
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final StorageRestAdapter<String> storageRestAdapter;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteBase(final StorageRestAdapter<String> embeddedStorageRestAdapter)
	{
		this.storageRestAdapter = embeddedStorageRestAdapter;
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public StorageRestAdapter<String> getStorageRestAdapter() {
		return this.storageRestAdapter;
	}
}
