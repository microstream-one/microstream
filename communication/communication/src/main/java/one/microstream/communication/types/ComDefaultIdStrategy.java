package one.microstream.communication.types;

/*-
 * #%L
 * microstream-communication
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

import one.microstream.persistence.internal.CompositeIdProvider;
import one.microstream.persistence.types.PersistenceIdStrategy;
import one.microstream.persistence.types.PersistenceObjectIdStrategy;
import one.microstream.persistence.types.PersistenceTypeIdStrategy;

public final class ComDefaultIdStrategy implements PersistenceIdStrategy
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static ComDefaultIdStrategy New(final long startingObjectId)
	{
		return new ComDefaultIdStrategy(
			PersistenceTypeIdStrategy.None(),
			PersistenceObjectIdStrategy.Transient(startingObjectId)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceTypeIdStrategy.None        typeIdStrategy  ;
	private final PersistenceObjectIdStrategy.Transient objectIdStrategy;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	ComDefaultIdStrategy(
		final PersistenceTypeIdStrategy.None        typeIdStrategy  ,
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
	public PersistenceTypeIdStrategy.None typeIdStragegy()
	{
		return this.typeIdStrategy;
	}
	
	public final long startingObjectId()
	{
		return this.objectIdStragegy().startingObjectId();
	}
	
	public CompositeIdProvider createIdProvider()
	{
		return CompositeIdProvider.New(
			this.createTypeIdProvider(),
			this.createObjectIdProvider()
		);
	}
	
}
