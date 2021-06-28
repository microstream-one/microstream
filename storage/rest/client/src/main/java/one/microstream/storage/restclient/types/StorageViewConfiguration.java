
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

public interface StorageViewConfiguration extends ValueRenderer.Provider
{
	public long elementRangeMaximumLength();
	
	public long maxValueLength();
	
	
	public static StorageViewConfiguration Default()
	{
		return new StorageViewConfiguration.Default(
			100,
			10_000,
			ValueRenderer.DefaultProvider()
		);
	}
	
	public static StorageViewConfiguration New(
		final long elementRangeMaximumLength,
		final long maxValueLength,
		final ValueRenderer.Provider valueRendererProvider
	)
	{
		return new StorageViewConfiguration.Default(
			elementRangeMaximumLength,
			maxValueLength,
			valueRendererProvider
		);
	}
	
	
	public static class Default implements StorageViewConfiguration
	{
		private final long                   elementRangeMaximumLength;
		private final long                   maxValueLength;
		private final ValueRenderer.Provider valueRendererProvider;
		
		Default(
			final long elementRangeMaximumLength,
			final long maxValueLength,
			final ValueRenderer.Provider valueRendererProvider
		)
		{
			super();
			this.elementRangeMaximumLength = elementRangeMaximumLength;
			this.maxValueLength            = maxValueLength;
			this.valueRendererProvider     = valueRendererProvider;
		}
		
		@Override
		public long elementRangeMaximumLength()
		{
			return this.elementRangeMaximumLength;
		}

		@Override
		public long maxValueLength()
		{
			return this.maxValueLength;
		}
		
		@Override
		public ValueRenderer provideValueRenderer(
			final String typeName
		)
		{
			return this.valueRendererProvider.provideValueRenderer(typeName);
		}
		
	}
	
}
