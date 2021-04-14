module microstream.persistence.binary.jdk8
{
	exports one.microstream.persistence.binary.jdk8.types;
	exports one.microstream.persistence.binary.jdk8.java.util;
	
	requires jdk.unsupported;
	requires microstream.base;
	requires microstream.persistence;
	requires microstream.persistence.binary;
}
