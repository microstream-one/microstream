package one.microstream.persistence.types;

import org.slf4j.Logger;

import one.microstream.util.logging.Logging;

/**
 * A PersistenceLegacyTypeHandlerCreator<> implementation that creates {@link PersistenceLegacyTypeHandlerUpdating}
 * instances.
 *
 */
public class PersistenceLegacyTypeHandlerUpdatingCreator<D> implements PersistenceLegacyTypeHandlerCreator<D>
{
	private final static Logger logger = Logging.getLogger(PersistenceLegacyTypeHandlerUpdatingCreator.class);
	
	public static <D> PersistenceLegacyTypeHandlerCreator<D> New(
		final PersistenceFoundation<D,?> connectionFoundation)
	{
		return new PersistenceLegacyTypeHandlerUpdatingCreator<D>(connectionFoundation.getLegacyTypeHandlerCreator());
	}
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceLegacyTypeHandlerCreator<D> creator;
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public PersistenceLegacyTypeHandlerUpdatingCreator(final PersistenceLegacyTypeHandlerCreator<D> creator)
	{
		super();
		this.creator = creator;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public <T> PersistenceLegacyTypeHandler<D, T> createLegacyTypeHandler(
		final PersistenceLegacyTypeMappingResult<D, T> mappingResult)
	{
		logger.debug("creating wrapper for {}", mappingResult.legacyTypeDefinition().typeName());
		
		return new PersistenceLegacyTypeHandlerUpdating<D, T>(this.creator.createLegacyTypeHandler(mappingResult));
	}
	
}
