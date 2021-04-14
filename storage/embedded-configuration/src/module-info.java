module microstream.storage.embedded.configuration
{
	exports one.microstream.storage.configuration;
	exports one.microstream.storage.embedded.configuration;
	
	requires java.xml;
	requires microstream.afs;
	requires microstream.afs.nio;
	requires microstream.base;
	requires microstream.configuration;
	requires microstream.persistence;
	requires microstream.persistence.binary;
	requires microstream.storage;
	requires microstream.storage.embedded;
}
