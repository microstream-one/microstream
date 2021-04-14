module microstream.cache
{
	exports one.microstream.cache.types;
	exports one.microstream.cache.exceptions;
	
	requires cache.api;
	requires java.management;
	requires microstream.afs;
	requires microstream.base;
	requires microstream.configuration;
	requires microstream.persistence;
	requires microstream.persistence.binary;
	requires microstream.storage;
	requires microstream.storage.embedded;
	requires microstream.storage.embedded.configuration;
}
