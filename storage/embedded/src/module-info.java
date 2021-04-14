module microstream.storage.embedded
{
	exports one.microstream.storage.embedded.types;
	
	requires microstream.afs;
	requires microstream.afs.nio;
	requires microstream.base;
	requires microstream.persistence;
	requires microstream.persistence.binary;
	requires microstream.storage;
}
