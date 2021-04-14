module microstream.persistence.binary
{
	exports one.microstream.persistence.binary.java.util.regex;
	exports one.microstream.persistence.binary.java.util;
	exports one.microstream.persistence.binary.java.time;
	exports one.microstream.persistence.binary.types;
	exports one.microstream.persistence.binary.java.math;
	exports one.microstream.persistence.binary.internal;
	exports one.microstream.persistence.binary.java.sql;
	exports one.microstream.persistence.binary.one.microstream.util;
	exports one.microstream.persistence.binary.exceptions;
	exports one.microstream.persistence.binary.java.lang;
	exports one.microstream.persistence.binary.one.microstream.reference;
	exports one.microstream.persistence.binary.java.io;
	exports one.microstream.persistence.binary.java.nio.file;
	exports one.microstream.persistence.binary.java.net;
	exports one.microstream.persistence.binary.java.util.concurrent;
	exports one.microstream.persistence.binary.one.microstream.entity;
	exports one.microstream.persistence.binary.one.microstream.persistence.types;
	exports one.microstream.persistence.binary.one.microstream.collections;
	
	requires java.sql;
	requires jdk.unsupported;
	requires microstream.afs;
	requires microstream.base;
	requires microstream.persistence;
}
