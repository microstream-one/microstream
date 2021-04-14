module microstream.storage.restclient.jersey
{
	exports one.microstream.storage.restclient.jersey.types;
	
	requires com.google.gson;
	requires java.ws.rs;
	requires microstream.base;
	requires microstream.persistence;
	requires microstream.persistence.binary;
	requires microstream.storage.restadapter;
	requires microstream.storage.restclient;
}
