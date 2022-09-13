package one.microstream.persistence.types;

/*-
 * #%L
 * MicroStream Persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
