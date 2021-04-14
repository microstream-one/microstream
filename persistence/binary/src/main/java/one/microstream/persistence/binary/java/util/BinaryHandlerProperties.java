package one.microstream.persistence.binary.java.util;

import java.util.Properties;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


/**
 * Due to the usual incompetence in the JDK, this type handler cannot handle a {@link Properties}' defaults values.
 * They simply left no (reasonable and JDK-independant) way to query the defaults of a certain {@link Properties}
 * instance.<p>
 * For a type handler that provides this functionality, albeit specific to JDK 8 (and higher but still compatible JDKs),
 * see {@literal one.microstream.jdk8.java.util.BinaryHandlerProperties}.
 * 
 * 
 */
public final class BinaryHandlerProperties extends AbstractBinaryHandlerMap<Properties>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerProperties New()
	{
		return new BinaryHandlerProperties();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerProperties()
	{
		super(Properties.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final Properties create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new Properties();
	}
	
}
