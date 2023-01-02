package one.microstream.entity;

/*-
 * #%L
 * microstream-base
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

import static one.microstream.X.mayNull;

/**
 * Abstract base class for chained entity layers.
 *  <p/>
 *  FH
 */
public abstract class EntityLayer extends Entity.AbstractAccessible
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	/*
	 * Layers are restored by BinaryHandlerEntityLayerIdentity
	 */
	private transient Entity inner;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected EntityLayer(final Entity inner)
	{
		super();
		this.inner = mayNull(inner); // may be null in case of delayed initialization.
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	protected Entity entityIdentity()
	{
		// the data instance (and only that) has a back-reference to the actual entity instance it belongs to.
		return Entity.identity(this.inner);
	}
	
	@Override
	protected Entity entityData()
	{
		return Entity.data(this.inner);
	}
	
	@Override
	protected void entityCreated()
	{
		Entity.Creator.Static.entityCreated(this.inner);
	}
	
	@Override
	protected boolean updateEntityData(final Entity newData)
	{
		/*
		 *  if the inner layer instance reports success, it is an intermediate layer.
		 *  Otherwise, it is the data itself and needs to be replaced.
		 */
		if(!Entity.updateData(this.inner, newData))
		{
			this.updateDataValidating(newData);
		}
		
		return true;
	}
	
	protected Entity inner()
	{
		return this.inner;
	}
	
	protected void validateNewData(final Entity newData)
	{
		this.validateIdentity(newData);
	}
	
	protected void updateDataValidating(final Entity newData)
	{
		final Entity actualNewData = Entity.data(newData);
		this.validateNewData(actualNewData);
		this.setInner(actualNewData);
	}
	
	protected void setInner(final Entity inner)
	{
		this.inner = inner;
	}
	
}
