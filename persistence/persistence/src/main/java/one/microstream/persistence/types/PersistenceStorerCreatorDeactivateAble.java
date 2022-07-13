package one.microstream.persistence.types;

import org.slf4j.Logger;

import one.microstream.reference.ObjectSwizzling;
import one.microstream.util.BufferSizeProviderIncremental;
import one.microstream.util.logging.Logging;

/**
 * PeristenceStorer creator that creates {@link one.microstream.persistence.types.PersistenceStorerDeactivateAble#PersistenceStorerDeactivateAble PersistenceStorerDeactivateAble}
 * instances.
 * 
 */
public class PersistenceStorerCreatorDeactivateAble<D> implements PersistenceStorer.Creator<D>
{
	private final static Logger logger = Logging.getLogger(PersistenceStorerCreatorDeactivateAble.class);
	
	public static <D> PersistenceStorerCreatorDeactivateAble<D> New(
		final PersistenceFoundation<D,?> connectionFoundation,
		final StorerModeController       storerModeController
	)
	{
		return new PersistenceStorerCreatorDeactivateAble<>(
			connectionFoundation.getStorerCreator(),
			storerModeController
		);
	}


	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceStorer.Creator<D> creator;
	private final StorerModeController         storerModeController;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceStorerCreatorDeactivateAble(
		final PersistenceStorer.Creator<D> creator,
		final StorerModeController         storerModeController
	)
	{
		super();
		this.creator              = creator;
		this.storerModeController = storerModeController;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public PersistenceStorer createLazyStorer(
		final PersistenceTypeHandlerManager<D> typeManager       ,
		final PersistenceObjectManager<D>      objectManager     ,
		final ObjectSwizzling                  objectRetriever   ,
		final PersistenceTarget<D>             target            ,
		final BufferSizeProviderIncremental    bufferSizeProvider
	)
	{
		logger.debug("Creating lazy storer");
		
		return this.storerModeController.register(
			new PersistenceStorerDeactivateAble(
				this.creator.createLazyStorer(
					typeManager,
					objectManager,
					objectRetriever,
					target,
					bufferSizeProvider)));
	}

	@Override
	public PersistenceStorer createEagerStorer(
		final PersistenceTypeHandlerManager<D> typeManager       ,
		final PersistenceObjectManager<D>      objectManager     ,
		final ObjectSwizzling                  objectRetriever   ,
		final PersistenceTarget<D>             target            ,
		final BufferSizeProviderIncremental    bufferSizeProvider
		)
	{
		logger.debug("Creating eager storer");
		
		return this.storerModeController.register(
			new PersistenceStorerDeactivateAble(
				this.creator.createEagerStorer(
					typeManager,
					objectManager,
					objectRetriever,
					target,
					bufferSizeProvider)));
	}

}
