package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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

import one.microstream.math.XMath;

@FunctionalInterface
public interface StorageInitialDataFileNumberProvider
{
	public int provideInitialDataFileNumber(int channelIndex);
	
	
	
	public final class Default implements StorageInitialDataFileNumberProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int constantInitialFileNumber;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default(final int constantInitialFileNumber)
		{
			super();
			this.constantInitialFileNumber = XMath.notNegative(constantInitialFileNumber);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public int provideInitialDataFileNumber(final int channelIndex)
		{
			return this.constantInitialFileNumber;
		}
		
	}
	
}
