package one.microstream.java.nio.file;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;


/* (27.11.2019 TM)FIXME: BinaryHandlerPath
 * This thing is not trivial to use.
 * See priv#185, priv#186, priv#187.
 * Until further notice, Path remains unhandled.
 */
public final class BinaryHandlerPath extends AbstractBinaryHandlerCustomValueVariableLength<Path, String>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerPath New()
	{
		return new BinaryHandlerPath();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerPath()
	{
		super(
			Path.class,
			CustomFields(
				chars("uri")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static String instanceState(final Path instance)
	{
		return instance.toUri().toString();
	}
	
	private static String binaryState(final Binary data)
	{
		return data.buildString();
	}

	@Override
	public void store(
		final Binary                  data    ,
		final Path                    instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// uri starts with a schema specification that basically defines the type/implementation of the path.
		data.storeStringSingleValue(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public Path create(final Binary data, final PersistenceLoadHandler handler)
	{
		// the URI schema is responsible to trigger the correct resolving and produce an instance of the right type.
		return Paths.get(URI.create(binaryState(data)));
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	@Override
	public String getValidationStateFromInstance(final Path instance)
	{
		return instanceState(instance);
	}

	@Override
	public String getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}

}
