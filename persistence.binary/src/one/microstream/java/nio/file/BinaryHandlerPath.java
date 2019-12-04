package one.microstream.java.nio.file;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;


/* (27.11.2019 TM)FIXME: BinaryHandlerPath
 * This thing is not trivial to use.
 * See priv#185, priv#186, priv#187.
 * Until further notice, Path remains unhandled.
 */
public final class BinaryHandlerPath extends AbstractBinaryHandlerCustomValueVariableLength<Path>
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

	@Override
	public void store(
		final Binary                  bytes   ,
		final Path                    instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// uri starts with a schema specification that basically defines the type/implementation of the path.
		bytes.storeStringValue(this.typeId(), objectId, instance.toUri().toString());
	}

	@Override
	public Path create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		// the URI schema is responsible to trigger the correct resolving and produce an instance of the right type.
		return Paths.get(URI.create(bytes.buildString()));
	}

}
