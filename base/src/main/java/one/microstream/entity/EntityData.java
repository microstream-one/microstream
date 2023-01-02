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

/**
 * Immutable entity data layer.
 * <p/>
 * FH
 */
public abstract class EntityData extends Entity.AbstractAccessible
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Entity entity;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected EntityData(final Entity entity)
	{
		super();
		this.entity = Entity.identity(entity);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	protected final Entity entityIdentity()
	{
		return this.entity;
	}
	
	@Override
	protected final Entity entityData()
	{
		return this;
	}
	
	@Override
	protected final void entityCreated()
	{
		// nothing to do here
	}
		
	@Override
	protected final boolean updateEntityData(final Entity newData)
	{
		// updating an entity's data means to replace the data instance, not mutate it. Data itself is immutable.
		return false;
	}
}
