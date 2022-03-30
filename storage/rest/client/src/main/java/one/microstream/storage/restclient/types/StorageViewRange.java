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

public interface StorageViewRange extends StorageViewElement
{
	public long offset();
	
	public long length();
	
	
	public static class Default extends StorageViewElement.Abstract implements StorageViewRange
	{
		private final long               objectId;
		private final long               offset;
		private final long               length;
		private List<StorageViewElement> members;
		
		Default(
			final StorageView.Default view,
			final StorageViewElement parent,
			final String name,
			final long objectId,
			final long offset,
			final long length
		)
		{
			super(view, parent, name, null, null);
			this.objectId = objectId;
			this.offset   = offset;
			this.length   = length;
		}

		@Override
		public long offset()
		{
			return this.offset;
		}

		@Override
		public long length()
		{
			return this.length;
		}
		
		@Override
		public boolean hasMembers()
		{
			return true;
		}
		
		@Override
		public List<StorageViewElement> members(final boolean forceRefresh)
		{
			if(this.members == null || forceRefresh)
			{
				this.members = this.view().variableMembers(
					this,
					this.objectId,
					this.offset,
					this.length
				);
			}
			return this.members;
		}
		
	}
	
}
