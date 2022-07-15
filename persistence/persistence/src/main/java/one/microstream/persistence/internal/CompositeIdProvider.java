package one.microstream.persistence.internal;

/*-
 * #%L
 * microstream-persistence
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

import static one.microstream.X.notNull;

import one.microstream.persistence.types.PersistenceObjectIdProvider;
import one.microstream.persistence.types.PersistenceTypeIdProvider;

public final class CompositeIdProvider implements PersistenceObjectIdProvider, PersistenceTypeIdProvider
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static CompositeIdProvider New(
		final PersistenceTypeIdProvider   typeIdProvider  ,
		final PersistenceObjectIdProvider objectIdProvider
	)
	{
		return new CompositeIdProvider(
			notNull(typeIdProvider)  ,
			notNull(objectIdProvider)
		);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceTypeIdProvider   typeIdProvider  ;
	private final PersistenceObjectIdProvider objectIdProvider;

	private transient boolean initialized;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	CompositeIdProvider(
		final PersistenceTypeIdProvider   typeIdProvider  ,
		final PersistenceObjectIdProvider objectIdProvider
	)
	{
		super();
		this.typeIdProvider   = typeIdProvider  ;
		this.objectIdProvider = objectIdProvider;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private void markInitialized()
	{
		this.initialized = true;
	}

	private boolean isInitialized()
	{
		return this.initialized;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public final synchronized CompositeIdProvider initialize()
	{
		if(!this.isInitialized())
		{
			this.typeIdProvider.initializeTypeId();
			this.objectIdProvider.initializeObjectId();
			this.markInitialized();
		}
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final CompositeIdProvider initializeTypeId()
	{
		return this.initialize();
	}

	@Override
	public final CompositeIdProvider initializeObjectId()
	{
		return this.initialize();
	}

	@Override
	public final long currentObjectId()
	{
		return this.objectIdProvider.currentObjectId();
	}

	@Override
	public final long currentTypeId()
	{
		return this.typeIdProvider.currentTypeId();
	}

	@Override
	public final long provideNextTypeId()
	{
		return this.typeIdProvider.provideNextTypeId();
	}

	@Override
	public final long provideNextObjectId()
	{
		return this.objectIdProvider.provideNextObjectId();
	}

	@Override
	public final CompositeIdProvider updateCurrentObjectId(final long currentObjectId)
	{
		this.objectIdProvider.updateCurrentObjectId(currentObjectId);
		return this;
	}

	@Override
	public final CompositeIdProvider updateCurrentTypeId(final long currentTypeId)
	{
		this.typeIdProvider.updateCurrentTypeId(currentTypeId);
		return this;
	}

}
