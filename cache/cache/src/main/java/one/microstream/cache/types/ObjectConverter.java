
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

public interface ObjectConverter
{
	public <T> Object internalize(T value);
	
	public <T> T externalize(Object internal);
	
	
	public static ObjectConverter ByReference()
	{
		return new ByReference();
	}
	
	public static ObjectConverter ByValue(final Serializer serializer)
	{
		return new ByValue(serializer);
	}
	
	
	public static class ByReference implements ObjectConverter
	{
		ByReference()
		{
			super();
		}
		
		@Override
		public <T> Object internalize(final T value)
		{
			return value;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T externalize(final Object internal)
		{
			return (T)internal;
		}
		
	}
	
	public static class ByValue implements ObjectConverter
	{
		private final Serializer serializer;
		
		ByValue(final Serializer serializer)
		{
			super();
			
			this.serializer = serializer;
		}
		
		@Override
		public <T> Object internalize(final T value)
		{
			return SerializedObject.New(
				value.hashCode(),
				this.serializer.write(value)
			);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T externalize(final Object internal)
		{
			return (T)this.serializer.read(
				((SerializedObject)internal).serializedData()
			);
		}
		
	}
	
}
