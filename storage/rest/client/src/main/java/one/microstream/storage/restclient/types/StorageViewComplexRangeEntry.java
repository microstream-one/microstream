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

public interface StorageViewComplexRangeEntry extends StorageViewElement
{
	public static class Default extends StorageViewElement.Abstract implements StorageViewComplexRangeEntry
	{
		private final List<StorageViewElement> members;
		
		Default(
			final StorageView.Default view,
			final StorageViewElement parent,
			final String name,
			final String value,
			final List<StorageViewElement> members
		)
		{
			super(view, parent, name, value, null);
			
			this.members = members;
		}
		
		@Override
		public boolean hasMembers()
		{
			return this.members.size() > 0;
		}
		
		@Override
		public List<StorageViewElement> members(
			final boolean forceRefresh
		)
		{
			return this.members;
		}
		
	}
	
}
