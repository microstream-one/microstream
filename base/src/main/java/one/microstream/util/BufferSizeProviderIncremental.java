package one.microstream.util;

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

import one.microstream.math.XMath;
import one.microstream.typing.Immutable;
import one.microstream.typing.Stateless;


public interface BufferSizeProviderIncremental extends BufferSizeProvider
{
	public default long provideIncrementalBufferSize()
	{
		return this.provideBufferSize();
	}



	public final class Default implements BufferSizeProviderIncremental, Stateless
	{
		// since default methods, java is missing interface instantiation
	}
	
	
	public static BufferSizeProviderIncremental New()
	{
		return new BufferSizeProviderIncremental.Default();
	}
	
	public static BufferSizeProviderIncremental New(final long bufferSize)
	{
		return New(bufferSize, bufferSize);
	}
	
	public static BufferSizeProviderIncremental New(final long initialBufferSize, final long incrementalBufferSize)
	{
		return new BufferSizeProviderIncremental.Sized(
			XMath.positive(initialBufferSize),
			XMath.positive(incrementalBufferSize)
		);
	}
	
	public final class Sized implements BufferSizeProviderIncremental, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final long initialBufferSize    ;
		private final long incrementalBufferSize;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Sized(final long initialBufferSize, final long incrementalBufferSize)
		{
			super();
			this.initialBufferSize     = initialBufferSize    ;
			this.incrementalBufferSize = incrementalBufferSize;
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long provideBufferSize()
		{
			return this.initialBufferSize;
		}

		@Override
		public final long provideIncrementalBufferSize()
		{
			return this.incrementalBufferSize;
		}

	}

}
