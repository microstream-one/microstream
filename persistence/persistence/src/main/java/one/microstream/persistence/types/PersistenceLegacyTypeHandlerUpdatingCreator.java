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
