package one.microstream.storage.restclient.types;

/*-
 * #%L
 * microstream-storage-restclient
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import java.util.Collections;
import java.util.List;

public interface StorageViewValue extends StorageViewElement
{
	public static class Default extends StorageViewElement.Abstract implements StorageViewValue
	{
		Default(
			final StorageView.Default view,
			final StorageViewElement parent,
			final String name,
			final String value,
			final String typeName
		)
		{
			super(view, parent, name, value, typeName);
		}
		
		@Override
		public boolean hasMembers()
		{
			return false;
		}
		
		@Override
		public List<StorageViewElement> members(
			final boolean forceRefresh
		)
		{
			return Collections.emptyList();
		}
		
	}
	
}
