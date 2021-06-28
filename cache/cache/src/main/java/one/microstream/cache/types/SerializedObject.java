
package one.microstream.cache.types;

/*-
 * #%L
 * microstream-cache
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

import static one.microstream.X.notNull;

import one.microstream.persistence.binary.types.Binary;


public interface SerializedObject extends ByteSized
{
	public Binary serializedData();
	
	public static SerializedObject New(final int hashCode, final Binary serializedData)
	{
		return new Default(hashCode, serializedData);
	}
	
	public static class Default implements SerializedObject
	{
		private final int    hashCode;
		private final Binary serializedData;
		
		Default(final int hashCode, final Binary serializedData)
		{
			super();
			
			this.hashCode       = hashCode;
			this.serializedData = notNull(serializedData);
		}
		
		@Override
		public Binary serializedData()
		{
			return this.serializedData;
		}
		
		@Override
		public long byteSize()
		{
			return this.serializedData.totalLength();
		}
		
		@Override
		public int hashCode()
		{
			return this.hashCode;
		}
		
		@Override
		public boolean equals(final Object obj)
		{
			return obj == this
				|| (   obj instanceof SerializedObject
				    && obj.hashCode() == this.hashCode
				   );
		}
		
	}
	
}
