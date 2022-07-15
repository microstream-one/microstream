package one.microstream.communication.binarydynamic;

/*-
 * #%L
 * MicroStream Communication Binary
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

import one.microstream.persistence.types.PersistenceIdStrategy;
import one.microstream.persistence.types.PersistenceObjectIdStrategy;
import one.microstream.persistence.types.PersistenceTypeIdStrategy;

public class ComDynamicIdStrategy implements PersistenceIdStrategy
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static ComDynamicIdStrategy New(final long startingObjectId)
	{
		return new ComDynamicIdStrategy(
			PersistenceTypeIdStrategy.Transient() ,
			PersistenceObjectIdStrategy.Transient(startingObjectId)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceTypeIdStrategy.Transient   typeIdStrategy  ;
	private final PersistenceObjectIdStrategy.Transient objectIdStrategy;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	ComDynamicIdStrategy(
		final PersistenceTypeIdStrategy.Transient   typeIdStrategy  ,
		final PersistenceObjectIdStrategy.Transient objectIdStrategy
	)
	{
		super();
		this.typeIdStrategy   = typeIdStrategy  ;
		this.objectIdStrategy = objectIdStrategy;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public PersistenceObjectIdStrategy.Transient objectIdStragegy()
	{
		return this.objectIdStrategy;
	}

	@Override
	public PersistenceTypeIdStrategy.Transient typeIdStragegy()
	{
		return this.typeIdStrategy;
	}

}
