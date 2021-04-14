module microstream.storage
{
	exports one.microstream.storage.util;
	exports one.microstream.storage.types;
	exports one.microstream.storage.exceptions;
	
	requires microstream.afs;
	requires microstream.afs.nio;
	requires microstream.base;
	requires microstream.persistence;
	requires microstream.persistence.binary;
}
