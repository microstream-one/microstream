package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

public interface PersistenceSizedArrayLengthController
{
	public int controlArrayLength(int specifiedCapacity, int actualElementCount);
	
	
	
	/**
	 * Recommended for storing data (does not change program behavior).
	 * @return an unrestricted array length controller
	 * 
	 */
	public static PersistenceSizedArrayLengthController.Unrestricted Unrestricted()
	{
		return new PersistenceSizedArrayLengthController.Unrestricted();
	}
	
	/**
	 * Recommended for communication (prevents array bombs).
	 * @return a fitting array length controller
	 */
	public static PersistenceSizedArrayLengthController.Fitting Fitting()
	{
		return new PersistenceSizedArrayLengthController.Fitting();
	}
	
	public static PersistenceSizedArrayLengthController.Limited Limited(final int maximumCapacity)
	{
		return new PersistenceSizedArrayLengthController.Limited(
			XMath.positive(maximumCapacity)
		);
	}
	
	public final class Unrestricted implements PersistenceSizedArrayLengthController
	{
		Unrestricted()
		{
			super();
		}

		@Override
		public final int controlArrayLength(final int specifiedCapacity, final int actualElementCount)
		{
			return specifiedCapacity;
		}
		
	}
	
	public final class Fitting implements PersistenceSizedArrayLengthController
	{
		Fitting()
		{
			super();
		}

		@Override
		public final int controlArrayLength(final int specifiedCapacity, final int actualElementCount)
		{
			return actualElementCount;
		}
		
	}
	
	public final class Limited implements PersistenceSizedArrayLengthController
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int limit;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Limited(final int limit)
		{
			super();
			this.limit = limit;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public final int limit()
		{
			return this.limit;
		}

		@Override
		public final int controlArrayLength(final int specifiedCapacity, final int actualElementCount)
		{
			return Math.min(specifiedCapacity, Math.max(this.limit, actualElementCount));
		}
		
	}
	
}
