
package one.microstream.storage.restclient.types;

/*-
 * #%L
 * microstream-storage-restclient
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

import java.util.List;

import one.microstream.persistence.types.PersistenceTypeDescription;


public interface StorageViewObject extends StorageViewValue
{
	public PersistenceTypeDescription typeDescription();
	
	public long objectId();
	
	
	public static class Simple extends StorageViewValue.Default implements StorageViewObject
	{
		final PersistenceTypeDescription typeDescription;
		final long                       objectId;
		
		Simple(
			final StorageView.Default view,
			final StorageViewElement parent,
			final String name,
			final String value,
			final PersistenceTypeDescription typeDescription,
			final long objectId
		)
		{
			super(view, parent, name, value, typeDescription.typeName());
			
			this.typeDescription = typeDescription;
			this.objectId        = objectId;
		}
		
		@Override
		public PersistenceTypeDescription typeDescription()
		{
			return this.typeDescription;
		}

		@Override
		public long objectId()
		{
			return this.objectId;
		}
		
		@Override
		public String toString()
		{
			return super.toString() + " " + this.objectId;
		}
	}
	
	
	public static class Complex extends Simple
	{
		private List<StorageViewElement> members;
		
		Complex(
			final StorageView.Default view,
			final StorageViewElement parent,
			final String name,
			final String data,
			final PersistenceTypeDescription typeDescription,
			final long objectId
		)
		{
			super(view, parent, name, data, typeDescription, objectId);
		}
		
		@Override
		public boolean hasMembers()
		{
			return this.typeDescription.allMembers().size() > 0;
		}
		
		@Override
		public List<StorageViewElement> members(
			final boolean forceRefresh
		)
		{
			if(this.members == null || forceRefresh)
			{
				this.members = this.view().members(this);
			}
			return this.members;
		}
		
	}
	
}
